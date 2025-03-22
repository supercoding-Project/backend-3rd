CREATE TABLE chat_room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL, -- 채팅방 이름
    calendar_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL, -- 보낸 user_id
    message TEXT,
    file_url VARCHAR(255), -- 파일 첨부 시 파일 경로 저장 , S3 or 서버 내 저장
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_room_created (room_id, created_at) -- 조회 최적화
    -- FOREIGN KEY (room_id) REFERENCES chat_room(id) ON DELETE CASCADE
);

CREATE TABLE chat_room_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL, -- 채팅방에 들어온 user_id
    last_read_message_id BIGINT DEFAULT NULL, -- 마지막으로 읽은 message_id
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 참여 시간
    UNIQUE KEY uniq_room_user (room_id, user_id) -- 한 유저의 동일한 방 중복 참여 방지
);


