CREATE TABLE scheduler_invitation_alarm (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,  -- 알림을 받을 사용자
    calendar_id BIGINT NOT NULL,  -- 캘린더 ID
    type VARCHAR(45) NOT NULL,
    is_checked BOOLEAN DEFAULT FALSE,  -- 알림 읽음 여부
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 생성 시간
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  -- 수정 시간
);
