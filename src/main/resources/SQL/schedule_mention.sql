CREATE TABLE schedule_mention(
                                 mention_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 schedule_id BIGINT NOT NULL,
                                 user_id BIGINT NOT NULL,
                                 created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                                 #FOREIGN KEY (schedule_id) REFERENCES schedules(schedule_id),
                                 #FOREIGN KEY (user_id) REFERENCES users(user_id)
);