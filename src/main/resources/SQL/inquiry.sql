CREATE TABLE inquiry (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         title VARCHAR(255) NOT NULL,
                         content TEXT NOT NULL,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         answered BOOLEAN DEFAULT FALSE,
                         password VARCHAR(255) NOT NULL,
                         is_private BOOLEAN DEFAULT FALSE,
                         category ENUM('ACCOUNT', 'SCHEDULE', 'CHAT' ,'OTHER') NOT NULL,
                         user_id BIGINT,
                         FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);