-- ================================
-- 1. TẠO DATABASE
-- ================================
CREATE DATABASE chat_app
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE chat_app;

-- ================================
-- 2. BẢNG USERS
-- ================================
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ================================
-- 3. BẢNG CHAT GROUPS
-- ================================
CREATE TABLE chat_groups (
    id INT AUTO_INCREMENT PRIMARY KEY,
    group_name VARCHAR(100),
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_group_creator
        FOREIGN KEY (created_by)
        REFERENCES users(id)
        ON DELETE SET NULL
);

-- ================================
-- 4. BẢNG GROUP MEMBERS
-- ================================
CREATE TABLE group_members (
    id INT AUTO_INCREMENT PRIMARY KEY,
    group_id INT NOT NULL,
    user_id INT NOT NULL,

    CONSTRAINT fk_group_member_group
        FOREIGN KEY (group_id)
        REFERENCES chat_groups(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_group_member_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    UNIQUE (group_id, user_id)
);

-- ================================
-- 5. BẢNG MESSAGES
-- ================================
CREATE TABLE messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NULL,
    group_id INT NULL,
    content TEXT,
    message_type VARCHAR(20) DEFAULT 'TEXT',
    status VARCHAR(20) DEFAULT 'SENT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_message_sender
        FOREIGN KEY (sender_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_message_receiver
        FOREIGN KEY (receiver_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_message_group
        FOREIGN KEY (group_id)
        REFERENCES chat_groups(id)
        ON DELETE CASCADE
);

-- ================================
-- 6. INDEX TỐI ƯU TRUY VẤN
-- ================================
CREATE INDEX idx_message_sender ON messages(sender_id);
CREATE INDEX idx_message_receiver ON messages(receiver_id);
CREATE INDEX idx_message_group ON messages(group_id);
CREATE INDEX idx_message_time ON messages(created_at);
