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
CREATE TABLE `users` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `displayName` VARCHAR(100),
    `createdAt` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ================================
-- 3. BẢNG CHAT GROUPS
-- ================================
CREATE TABLE `chatGroups` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `groupName` VARCHAR(100),
    `createdBy` INT,
    `createdAt` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fkGroupCreator` FOREIGN KEY (`createdBy`) REFERENCES `users`(`id`) ON DELETE SET NULL
);

-- ================================
-- 4. BẢNG GROUP MEMBERS
-- ================================
CREATE TABLE `groupMembers` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `groupId` INT NOT NULL,
    `userId` INT NOT NULL,
    CONSTRAINT `fkMemberGroup` FOREIGN KEY (`groupId`) REFERENCES `chatGroups`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fkMemberUser` FOREIGN KEY (`userId`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    UNIQUE (`groupId`, `userId`)
);

-- ================================
-- 5. BẢNG MESSAGES
-- ================================
CREATE TABLE `messages` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `senderId` INT NOT NULL,
    `receiverId` INT NULL,
    `groupId` INT NULL,
    `content` TEXT,
    `messageType` VARCHAR(20) DEFAULT 'TEXT',
    `status` VARCHAR(20) DEFAULT 'SENT',
    `createdAt` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fkMessageSender` FOREIGN KEY (`senderId`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fkMessageReceiver` FOREIGN KEY (`receiverId`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fkMessageGroup` FOREIGN KEY (`groupId`) REFERENCES `chatGroups`(`id`) ON DELETE CASCADE
);

-- ================================
-- 6. INDEX TỐI ƯU TRUY VẤN
-- ================================
CREATE INDEX idx_message_sender ON messages(sender_id);
CREATE INDEX idx_message_receiver ON messages(receiver_id);
CREATE INDEX idx_message_group ON messages(group_id);
CREATE INDEX idx_message_time ON messages(created_at);
