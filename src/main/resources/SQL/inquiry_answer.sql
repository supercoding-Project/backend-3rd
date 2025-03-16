CREATE TABLE inquiry_answer (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                content TEXT NOT NULL,
                                answered_at TIMESTAMP NOT NULL,
                                updated_at TIMESTAMP,
                                admin_id BIGINT,
                                inquiry_id BIGINT,
                                parent_answer_id BIGINT,
                                FOREIGN KEY (admin_id) REFERENCES user(id),
                                FOREIGN KEY (inquiry_id) REFERENCES inquiry(id),
                                FOREIGN KEY (parent_answer_id) REFERENCES inquiry_answer(id)
);