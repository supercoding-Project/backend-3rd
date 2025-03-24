create table users(
                      user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      email VARCHAR(100) NOT NULL,
                      password VARCHAR(255) NOT NULL,
                      username VARCHAR(20) NOT NULL,
                      phone VARCHAR(30) NOT NULL,
                      provider VARCHAR(100),
                      provider_id BIGINT,
                      role VARCHAR(20) NOT NULL,
                      created_at DATETIME NOT NULL
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