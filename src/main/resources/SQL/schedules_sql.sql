CREATE TABLE schedules (
                           schedule_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           create_user_id BIGINT NOT NULL,
                           title VARCHAR(255) NOT NULL,
                           start_time DATETIME NOT NULL,
                           end_time DATETIME NOT NULL,
                           repeat_type ENUM('NONE', 'DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY') DEFAULT 'NONE',
                           repeat_interval INT DEFAULT 1,
                           repeat_end_date DATE,
                           location VARCHAR(255),
                           memo TEXT,
                           team_code VARCHAR(50),  -- team_code가 NULL이면 개인 일정, 값이 있으면 팀(공유) 일정
                           schedule_status ENUM('SCHEDULED', 'COMPLETED', 'CANCELLED') DEFAULT 'SCHEDULED',
                           chat_room_id BIGINT,
                           created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                           updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           FOREIGN KEY (create_user_id) REFERENCES users(user_id)
);