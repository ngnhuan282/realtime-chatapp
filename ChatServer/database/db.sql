-- =============================================
-- 1. KHỞI TẠO DATABASE
-- =============================================
DROP DATABASE IF EXISTS chat_app;
CREATE DATABASE chat_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE chat_app;

-- =============================================
-- 2. BẢNG USERS (10 dòng)
-- =============================================
CREATE TABLE `users` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `displayName` VARCHAR(100),
    `createdAt` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO `users` (`username`, `password`, `displayName`) VALUES
('loopy', '123456', 'Loopy Cute'),
('alice', '123456', 'Alice Green'),
('mark', '123456', 'Mark Rivers'),
('sarah', '123456', 'Sarah Woods'),
('david', '123456', 'David Chen'),
('emily', '123456', 'Emily Rose'),
('james', '123456', 'James Lee'),
('marcus', '123456', 'Marcus Johnson'),
('anna', '123456', 'Anna Smith'),
('johnny', '123456', 'Johnny Depp');

-- =============================================
-- 3. BẢNG CHAT GROUPS (10 dòng)
-- =============================================
CREATE TABLE `chatGroups` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `groupName` VARCHAR(100),
    `createdBy` INT,
    `createdAt` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fkGroupCreator` FOREIGN KEY (`createdBy`) REFERENCES `users`(`id`) ON DELETE SET NULL
);

INSERT INTO `chatGroups` (`groupName`, `createdBy`) VALUES
('Gia Đình', 1),
('Hội Anh Em', 3),
('Team Android', 1),
('Lớp Đại Học', 5),
('Công Ty X', 2),
('CLB Cầu Lông', 7),
('Dân Dev Java', 1),
('Hội Ăn Đêm', 4),
('Nhóm Leo Núi', 8),
('Thảo Luận UI/UX', 6);

-- =============================================
-- 4. BẢNG GROUP MEMBERS (10 dòng)
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
(1, 1), (1, 2), (1, 3), -- Gia đình có Loopy, Alice, Mark
(2, 3), (2, 8), (2, 10), -- Hội anh em
(3, 1), (3, 4), (3, 5), -- Team Android
(4, 5), (4, 6), (4, 7); -- Lớp Đại học

-- =============================================
-- 5. BẢNG MESSAGES (10 dòng)
-- =============================================
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

INSERT INTO `messages` (`senderId`, `receiverId`, `groupId`, `content`, `messageType`, `status`) VALUES
(1, 2, NULL, 'Chào Alice, khỏe không?', 'TEXT', 'READ'),
(2, 1, NULL, 'Chào Loopy, mình khỏe!', 'TEXT', 'READ'),
(1, NULL, 1, 'Mọi người ăn tối chưa?', 'TEXT', 'SENT'),
(3, NULL, 1, 'Đang chuẩn bị ăn đây Loopy ơi.', 'TEXT', 'SENT'),
(4, 1, NULL, 'Gửi mình tài liệu Android với.', 'TEXT', 'DELIVERED'),
(5, 6, NULL, 'Hẹn gặp chiều nay nhé.', 'TEXT', 'SENT'),
(1, 3, NULL, 'Tối nay đi làm tí cafe không?', 'TEXT', 'READ'),
(3, 1, NULL, 'Ok luôn, quán cũ nhé.', 'TEXT', 'SENT'),
(7, NULL, 4, 'Khi nào có lịch thi vậy nhỉ?', 'TEXT', 'SENT'),
(1, NULL, 3, 'Check file APK mình mới gửi nhé.', 'TEXT', 'SENT');

-- =============================================
-- 6. INDEX TỐI ƯU
-- =============================================
CREATE INDEX idxMessageSender ON messages(senderId);
CREATE INDEX idxMessageReceiver ON messages(receiverId);
CREATE INDEX idxMessageGroup ON messages(groupId);