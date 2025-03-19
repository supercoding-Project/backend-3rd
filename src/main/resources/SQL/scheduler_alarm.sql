CREATE TABLE scheduler_alarm (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    calendar_id BIGINT NOT NULL,
    scheduler_id BIGINT NOT NULL,
    type String NOT NULL,
    is_checked BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
