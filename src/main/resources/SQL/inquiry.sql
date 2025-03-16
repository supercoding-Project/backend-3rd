CREATE TABLE inquiry (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         title VARCHAR(255) NOT NULL,
                         content TEXT NOT NULL,
                         created_at TIMESTAMP NOT NULL,
                         answered BOOLEAN DEFAULT FALSE,       --- 답변 여부
                         password VARCHAR(255) NOT NULL,
                         is_private BOOLEAN DEFAULT FALSE,      --- 공개 여부
                         category VARCHAR(255) NOT NULL,
                         user_id BIGINT,
                         FOREIGN KEY (user_id) REFERENCES user(id)
);