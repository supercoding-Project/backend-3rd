create table users(
                      user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      email VARCHAR(100) NOT NULL,
                      password VARCHAR(255) NOT NULL,
                      username VARCHAR(20) NOT NULL,
                      phone VARCHAR(30) NOT NULL,
                      provider VARCHAR(100),
                      provider_id BIGINT,
                      role VARCHAR(20) NOT NULL,
                      status VARCHAR(20) NOT NULL,
                      created_at DATETIME NOT NULL,
                      deleted_at DATETIME
);

create table user_image(
                           image_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           url VARCHAR(255) NOT NULL,
                           user_id BIGINT NOT NULL,
                           FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE refresh_token (
                               refresh_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_id BIGINT NOT NULL UNIQUE,
                               refresh_token VARCHAR(255) NOT NULL UNIQUE,
                               expiration TIMESTAMP NOT NULL,
                               FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE calendar (
                          calendar_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          calendar_name VARCHAR(255) NOT NULL,
                          owner_id BIGINT NOT NULL, -- 캘린더 생성자
                          calendar_type VARCHAR(20), -- 캘린더 타입 설정(개인, 공용, 할일)
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (owner_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE user_calendar (
                               user_calendar_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               user_id BIGINT NOT NULL,
                               calendar_id BIGINT NOT NULL,
                               role VARCHAR(20) NOT NULL,
                               joined_at DATETIME,
                               FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                               FOREIGN KEY (calendar_id) REFERENCES calendar(calendar_id) ON DELETE CASCADE,
                               UNIQUE (user_id, calendar_id) -- 중복 방지
);