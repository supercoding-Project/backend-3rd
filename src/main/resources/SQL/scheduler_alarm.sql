CREATE TABLE scheduler_alarm (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                user_id BIGINT NOT NULL,
                                calendar_id BIGINT NOT NULL,
                                scheduler_id BIGINT NOT NULL,
                                type VARCHAR(100) NOT NULL,
                                is_checked BOOLEAN DEFAULT FALSE,
                                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                FOREIGN KEY (user_id) REFERENCES users(user_id),
                                FOREIGN KEY (calendar_id) REFERENCES calendar(calendar_id),
                                FOREIGN KEY (scheduler_id) REFERENCES schedules(scheduler_id)
);

CREATE TABLE schedule_mention(
                                 mention_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 schedule_id BIGINT NOT NULL,
                                 user_id BIGINT NOT NULL,
                                 created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                                 FOREIGN KEY (schedule_id) REFERENCES schedules(schedule_id),
                                 FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE scheduler_invitation_alarm (
                                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            user_id BIGINT NOT NULL,  -- 알림을 받을 사용자
                                            calendar_id BIGINT NOT NULL,  -- 캘린더 ID
                                            type VARCHAR(45) NOT NULL,
                                            is_checked BOOLEAN DEFAULT FALSE,  -- 알림 읽음 여부
                                            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 생성 시간
                                            updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,  -- 수정 시간
                                            FOREIGN KEY (user_id) REFERENCES users(user_id),
                                            FOREIGN KEY (calendar_id) REFERENCES calendar(calendar_id)
);
