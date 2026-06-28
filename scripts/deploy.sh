set -e

# ─── 로그 헬퍼 ───────────────────────────────────────────
TIMESTAMP() { date '+%Y-%m-%d %H:%M:%S'; }
INFO()  { echo "[$(TIMESTAMP)] [INFO]  $*"; }
OK()    { echo "[$(TIMESTAMP)] [OK]    $*"; }
WARN()  { echo "[$(TIMESTAMP)] [WARN]  $*"; }
ERROR() { echo "[$(TIMESTAMP)] [ERROR] $*" >&2; }

trap 'ERROR "예상치 못한 오류 발생 (line $LINENO). 배포 중단."' ERR

# ─── 설정 ────────────────────────────────────────────────
IMAGE="haphapsopt/haphap-server:${IMAGE_TAG:?IMAGE_TAG is required}"
WORK_DIR="/home/ubuntu/haphap"

INFO "========================================"
INFO "  HAP HAP 배포 시작"
INFO "  IMAGE: $IMAGE"
INFO "========================================"

cd "$WORK_DIR"

# ─── Redis 실행 확인 ──────────────────────────────────────
INFO "[1/6] Redis 컨테이너 확인 중..."
docker compose up -d redis
OK "Redis 실행 중"

# ─── 블루/그린 결정 ───────────────────────────────────────
INFO "[2/6] 배포 슬롯 결정 중..."
if docker ps --format '{{.Names}}' | grep -q "^haphap-blue$"; then
  CURRENT="blue"; NEXT="green"; NEXT_PORT=8081
else
  CURRENT="green"; NEXT="blue"; NEXT_PORT=8080
fi
INFO "현재 활성: $CURRENT → 다음 배포: $NEXT (port $NEXT_PORT)"

# ─── 이미지 Pull ──────────────────────────────────────────
INFO "[3/6] Docker 이미지 pull 중..."
docker pull "$IMAGE"
OK "이미지 pull 완료"

# ─── 기존 컨테이너 정리 ──────────────────────────────────
INFO "[4/6] 기존 $NEXT 컨테이너 정리 중..."
docker stop "haphap-$NEXT" 2>/dev/null && INFO "기존 컨테이너 중지됨" || true
docker rm   "haphap-$NEXT" 2>/dev/null && INFO "기존 컨테이너 삭제됨" || true

# ─── 새 컨테이너 실행 ─────────────────────────────────────
INFO "[5/6] haphap-$NEXT 컨테이너 시작 중..."
docker run -d \
  --name "haphap-$NEXT" \
  --network haphap-net \
  -p "${NEXT_PORT}:8080" \
  --env-file "$WORK_DIR/.env" \
  -e REDIS_HOST=redis \
  --restart unless-stopped \
  "$IMAGE"
OK "컨테이너 시작됨"

# ─── Health Check ─────────────────────────────────────────
INFO "[6/6] Health check 중 (최대 30초)..."
for i in $(seq 1 15); do
  if curl -sf "http://localhost:${NEXT_PORT}/actuator/health" > /dev/null 2>&1; then
    OK "Health check 통과 (시도 $i/15)"
    break
  fi
  if [ "$i" -eq 15 ]; then
    ERROR "Health check 15회 모두 실패"
    ERROR "컨테이너 로그 출력 시작"
    docker logs "haphap-$NEXT" 2>&1 || true
    ERROR "컨테이너 로그 출력 종료"
    docker stop "haphap-$NEXT" || true
    docker rm   "haphap-$NEXT" || true
    exit 1
  fi
  WARN "시도 $i/15 실패, 2초 후 재시도..."
  sleep 2
done

# ─── Nginx 전환 ───────────────────────────────────────────
INFO "Nginx 포트 전환 중: → $NEXT_PORT"
sudo sed -i "s/127\.0\.0\.1:808[01]/127.0.0.1:${NEXT_PORT}/" /etc/nginx/conf.d/haphap.conf
sudo nginx -s reload
OK "Nginx 전환 완료 (port $NEXT_PORT)"

# ─── 이전 컨테이너 종료 ──────────────────────────────────
if docker ps --format '{{.Names}}' | grep -q "^haphap-$CURRENT$"; then
  INFO "이전 컨테이너(haphap-$CURRENT) 종료 중..."
  docker stop "haphap-$CURRENT"
  docker rm   "haphap-$CURRENT"
  OK "haphap-$CURRENT 종료 완료"
fi

INFO "========================================"
OK "  배포 완료! Active: haphap-$NEXT (port $NEXT_PORT)"
INFO "========================================"