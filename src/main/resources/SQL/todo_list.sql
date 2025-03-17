CREATE TABLE todo_list(
                          todo_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          create_user_id BIGINT NOT NULL,
                          schedule_id BIGINT NOT NULL,
                          todo_content VARCHAR(225) NOT NULL,
                          todo_date  DATE NOT NULL,
                          meno VARCHAR(225),
                          completed  BOOLEAN DEFAULT FALSE,
                          repeat_type ENUM('NONE', 'DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY') DEFAULT 'NONE',
                          repeat_interval INT DEFAULT 0,
                          repeat_end_date DATE,
                          created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                          updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          FOREIGN KEY (create_user_id) REFERENCES users(user_id),
                          FOREIGN KEY (schedule_id) REFERENCES schedules(schedule_id)
);
