CREATE TABLE schedules (
                           schedule_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           create_user_id BIGINT NOT NULL,  -- 캘린더 일정 등록자
                           title VARCHAR(255) NOT NULL,
                           start_time DATETIME NOT NULL,
                           end_time DATETIME NOT NULL,
                           repeat_type ENUM('NONE', 'DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY') DEFAULT 'NONE',
                           repeat_interval INT DEFAULT 0,
                           repeat_end_date DATE,
                           location VARCHAR(255),
                           memo TEXT,
                           calendar_id BIGINT,  -- team_code가 NULL이면 개인 일정, 값이 있으면 팀(공유) 일정
                           schedule_status ENUM('SCHEDULED', 'COMPLETED', 'CANCELLED') DEFAULT 'SCHEDULED',
                           created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                           updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           FOREIGN KEY (create_user_id) REFERENCES users(user_id),
                           FOREIGN KEY (calendar_id) REFERENCES calendar(calendar_id)
);
