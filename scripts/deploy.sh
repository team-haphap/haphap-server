set -e

IMAGE="haphapsopt/haphap-server:${IMAGE_TAG:?IMAGE_TAG is required}"
WORK_DIR="/home/ubuntu/haphap"

cd "$WORK_DIR"
docker compose up -d redis

if docker ps --format '{{.Names}}' | grep -q "^haphap-blue$"; then
  CURRENT="blue"
  NEXT="green"
  NEXT_PORT=8081
else
  CURRENT="green"
  NEXT="blue"
  NEXT_PORT=8080
fi

echo ">>> 배포 시작: $CURRENT → $NEXT (port $NEXT_PORT)"

docker pull "$IMAGE"

docker stop "haphap-$NEXT" 2>/dev/null || true
docker rm   "haphap-$NEXT" 2>/dev/null || true

if [ ! -f "$WORK_DIR/firebase-service-account.json" ]; then
  echo "Firebase 서비스 계정 파일이 없습니다: $WORK_DIR/firebase-service-account.json"
  exit 1
fi

docker run -d \
  --name "haphap-$NEXT" \
  --network haphap-net \
  -p "${NEXT_PORT}:8080" \
  --env-file "$WORK_DIR/.env" \
  -e REDIS_HOST=redis \
  -e FIREBASE_CONFIG_PATH=/app/firebase-service-account.json \
  -v "$WORK_DIR/firebase-service-account.json:/app/firebase-service-account.json:ro" \
  --restart unless-stopped \
  "$IMAGE"

# health check
echo ">>> haphap-$NEXT health check 중..."
for i in $(seq 1 15); do
  if curl -sf "http://localhost:${NEXT_PORT}/actuator/health" > /dev/null 2>&1; then
    echo "Health check 통과 (시도 $i)"
    break
  fi
  if [ "$i" -eq 15 ]; then
    echo "Health check 실패. 롤백합니다."
    docker stop "haphap-$NEXT" || true
    docker rm   "haphap-$NEXT" || true
    exit 1
  fi
  echo "  시도 $i 실패, 2초 후 재시도..."
  sleep 2
done

# nginx 포트 전환 (무중단배포)
sudo sed -i "s/127\.0\.0\.1:808[01]/127.0.0.1:${NEXT_PORT}/" /etc/nginx/conf.d/haphap.conf
sudo nginx -s reload
echo "Nginx → port $NEXT_PORT (무중단 전환 완료)"

if docker ps --format '{{.Names}}' | grep -q "^haphap-$CURRENT$"; then
  docker stop "haphap-$CURRENT"
  docker rm   "haphap-$CURRENT"
  echo "haphap-$CURRENT 종료"
fi

echo ">>> 사용하지 않는 이미지 정리 중..."
docker image prune -af --filter "until=24h" || true

echo "배포 완료! Active: haphap-$NEXT (port $NEXT_PORT)"