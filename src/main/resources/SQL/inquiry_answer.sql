CREATE TABLE inquiry_answer (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                content TEXT NOT NULL,
                                answered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                admin_id BIGINT,
                                inquiry_id BIGINT,
                                parent_answer_id BIGINT,
                                FOREIGN KEY (admin_id) REFERENCES users(user_id) ON DELETE SET NULL,
                                FOREIGN KEY (inquiry_id) REFERENCES inquiry(id) ON DELETE CASCADE,
                                FOREIGN KEY (parent_answer_id) REFERENCES inquiry_answer(id) ON DELETE CASCADE
);