CREATE TABLE todo_list (
                           todo_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           create_user_id BIGINT NOT NULL,
                           todo_content VARCHAR(225) NOT NULL,
                           todo_date DATE NOT NULL,
                           memo TEXT,
                           completed BOOLEAN DEFAULT FALSE,
                           repeat_type ENUM('NONE', 'DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY') DEFAULT 'NONE',
                           repeat_interval INT DEFAULT 0,
                           repeat_end_date DATE,
                           calendar_id BIGINT,
                           version BIGINT NOT NULL DEFAULT 0,
                           created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                           updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           FOREIGN KEY (create_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                           FOREIGN KEY (calendar_id) REFERENCES calendar(calendar_id) ON DELETE CASCADE
);