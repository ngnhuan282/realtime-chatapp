-- =============================================
-- 1. KHỞI TẠO DATABASE
-- =============================================
DROP DATABASE IF EXISTS chat_app;
CREATE DATABASE chat_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE chat_app;

-- =============================================
-- 2. BẢNG USERS
-- =============================================
CREATE TABLE `users` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `displayName` VARCHAR(100),
    `createdAt` BIGINT -- Lưu miliseconds cho đồng bộ Android
);

INSERT INTO `users` (`username`, `password`, `displayName`, `createdAt`) VALUES
('loopy', '123456', 'Loopy Cute', 1775463000000),
('alice', '123456', 'Alice Green', 1775463000000),
('mark', '123456', 'Mark Rivers', 1775463000000),
('sarah', '123456', 'Sarah Woods', 1775463000000),
('david', '123456', 'David Chen', 1775463000000),
('emily', '123456', 'Emily Rose', 1775463000000),
('james', '123456', 'James Lee', 1775463000000),
('marcus', '123456', 'Marcus Johnson', 1775463000000),
('anna', '123456', 'Anna Smith', 1775463000000),
('johnny', '123456', 'Johnny Depp', 1775463000000);

-- =============================================
-- 3. BẢNG CHAT GROUPS
-- =============================================
CREATE TABLE `chatGroups` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `groupName` VARCHAR(100),
    `createdBy` INT,
    `createdAt` BIGINT,
    CONSTRAINT `fkGroupCreator` FOREIGN KEY (`createdBy`) REFERENCES `users`(`id`) ON DELETE SET NULL
);

INSERT INTO `chatGroups` (`groupName`, `createdBy`, `createdAt`) VALUES
('Gia Đình', 1, 1775463100000),
('Team Android', 1, 1775463200000),
('Lớp Đại Học', 5, 1775463300000);

-- =============================================
-- 4. BẢNG GROUP MEMBERS
-- =============================================
CREATE TABLE `groupMembers` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `groupId` INT NOT NULL,
    `userId` INT NOT NULL,
    CONSTRAINT `fkMemberGroup` FOREIGN KEY (`groupId`) REFERENCES `chatGroups`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fkMemberUser` FOREIGN KEY (`userId`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    UNIQUE (`groupId`, `userId`)
);

INSERT INTO `groupMembers` (`groupId`, `userId`) VALUES
(1, 1), (1, 2), (1, 3),
(2, 1), (2, 4), (2, 5),
(3, 5), (3, 6), (3, 7);

-- =============================================
-- 5. BẢNG MESSAGES (Có thời gian mẫu chuẩn)
-- =============================================
CREATE TABLE `messages` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `senderId` INT NOT NULL,
    `receiverId` INT NULL,
    `groupId` INT NULL,
    `content` TEXT,
    `messageType` VARCHAR(20) DEFAULT 'TEXT',
    `status` VARCHAR(20) DEFAULT 'SENT',
    `createdAt` BIGINT, -- Trường này sẽ lưu miliseconds
    CONSTRAINT `fkMessageSender` FOREIGN KEY (`senderId`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fkMessageReceiver` FOREIGN KEY (`receiverId`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fkMessageGroup` FOREIGN KEY (`groupId`) REFERENCES `chatGroups`(`id`) ON DELETE CASCADE
);

-- Thêm dữ liệu mẫu với mốc thời gian thực (Chiều 06/04/2026)
INSERT INTO `messages` (`senderId`, `receiverId`, `groupId`, `content`, `messageType`, `status`, `createdAt`) VALUES
-- Chat với Alice (ID 2)
(1, 2, NULL, 'Chào Alice, khỏe không?', 'TEXT', 'READ', 1775463600000), -- 15:00
(2, 1, NULL, 'Chào Loopy, mình khỏe!', 'TEXT', 'READ', 1775463660000), -- 15:01
(1, 2, NULL, 'hjhj', 'TEXT', 'READ', 1775465220000),               -- 15:27
-- Chat với Mark (ID 3)
(1, 3, NULL, 'Tối nay đi làm tí cafe không?', 'TEXT', 'READ', 1775464140000), -- 15:09
(3, 1, NULL, 'Ok luôn, quán cũ nhé.', 'TEXT', 'SENT', 1775464200000),        -- 15:10
-- Chat với Sarah (ID 4)
(4, 1, NULL, 'Gửi mình tài liệu Android với.', 'TEXT', 'SENT', 1775464140000); -- 15:09

-- =============================================
-- 6. INDEX TỐI ƯU
-- =============================================
CREATE INDEX idxMessageSender ON messages(senderId);
CREATE INDEX idxMessageReceiver ON messages(receiverId);
CREATE INDEX idxMessageGroup ON messages(groupId);