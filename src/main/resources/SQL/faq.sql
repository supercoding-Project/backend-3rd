CREATE TABLE faq (
                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                     question VARCHAR(255) NOT NULL,
                     answer TEXT NOT NULL,
                     category ENUM('NOTICE', 'CALENDAR', 'SCHEDULE', 'USER', 'CHAT', 'INVITE', 'ETC') NOT NULL,
                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
