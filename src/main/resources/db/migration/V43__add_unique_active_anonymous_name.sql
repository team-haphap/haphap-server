-- 닉네임(anonymous_name) 중복 방지: 활성 유저끼리만 유일해야 한다.
-- 탈퇴(진행) 유저는 모두 '탈퇴한 사용자'로 바뀌므로 부분 인덱스로 제외한다.
CREATE UNIQUE INDEX uk_users_active_anonymous_name
    ON users (anonymous_name)
    WHERE withdrawal_status = 'ACTIVE';
