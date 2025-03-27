CREATE TABLE notice (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        title VARCHAR(255) NOT NULL,
                        content TEXT NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

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

CREATE TABLE faq (
                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                     question VARCHAR(255) NOT NULL,
                     answer TEXT NOT NULL,
                     category ENUM('NOTICE', 'CALENDAR', 'SCHEDULE', 'USER', 'CHAT', 'INVITE', 'ETC') NOT NULL,
                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);