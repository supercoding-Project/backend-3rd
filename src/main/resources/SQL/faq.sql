CREATE TABLE faq (
                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                     question VARCHAR(255) NOT NULL,
                     answer TEXT NOT NULL,
                     created_at TIMESTAMP NOT NULL,
                     updated_at TIMESTAMP
);