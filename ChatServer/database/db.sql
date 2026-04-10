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
                         `avatar` VARCHAR(255),
                         `phoneNumber` VARCHAR(20) UNIQUE,
                         `createdAt` BIGINT
);

-- Dữ liệu mẫu users
INSERT INTO `users` (`username`, `password`, `displayName`, `avatar`, `phoneNumber`, `createdAt`) VALUES
                                                                                                      ('loopy', '123456', 'Loopy Cute', NULL, '0912345678', 1775463000000),
                                                                                                      ('alice', '123456', 'Alice Green', NULL, '0987654321', 1775463000000),
                                                                                                      ('mark', '123456', 'Mark Rivers', NULL, '0978123456', 1775463000000),
                                                                                                      ('sarah', '123456', 'Sarah Woods', NULL, '0933456789', 1775463000000),
                                                                                                      ('david', '123456', 'David Chen', NULL, '0901122334', 1775463000000),
                                                                                                      ('emily', '123456', 'Emily Rose', NULL, '0945566778', 1775463000000),
                                                                                                      ('james', '123456', 'James Lee', NULL, '0919988776', 1775463000000),
                                                                                                      ('anna', '123456', 'Anna Smith', NULL, '0988776655', 1775463000000),
                                                                                                      ('johnny', '123456', 'Johnny Depp', NULL, '0933221100', 1775463000000),
                                                                                                      ('lisa', '123456', 'Lisa Blackpink', NULL, '0966554433', 1775463000000);

-- =============================================
-- 3. BẢNG FRIENDSHIPS
-- =============================================
CREATE TABLE `friendships` (
                               `id` INT AUTO_INCREMENT PRIMARY KEY,
                               `user1Id` INT NOT NULL,
                               `user2Id` INT NOT NULL,
                               `status` VARCHAR(20) DEFAULT 'PENDING',
                               `createdAt` BIGINT,
                               UNIQUE KEY `uk_friendship_pair` (`user1Id`, `user2Id`),
                               CONSTRAINT `fk_friendship_user1` FOREIGN KEY (`user1Id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
                               CONSTRAINT `fk_friendship_user2` FOREIGN KEY (`user2Id`) REFERENCES `users`(`id`) ON DELETE CASCADE
);

-- Dữ liệu mẫu friendships
INSERT INTO `friendships` (`user1Id`, `user2Id`, `status`, `createdAt`) VALUES
                                                                            (1, 2, 'ACCEPTED', 1775463000000),
                                                                            (1, 3, 'ACCEPTED', 1775463000000),
                                                                            (4, 1, 'PENDING', 1775463000000),
                                                                            (5, 1, 'PENDING', 1775463000000),
                                                                            (1, 6, 'ACCEPTED', 1775463000000),
                                                                            (2, 5, 'ACCEPTED', 1775463000000),
                                                                            (8, 2, 'PENDING', 1775463000000),
                                                                            (3, 7, 'ACCEPTED', 1775463000000);

-- =============================================
-- 4. BẢNG CHAT GROUPS
-- =============================================
CREATE TABLE `chatGroups` (
                              `id` INT AUTO_INCREMENT PRIMARY KEY,
                              `groupName` VARCHAR(100),
                              `createdBy` INT,
                              `createdAt` BIGINT,
                              CONSTRAINT `fkGroupCreator` FOREIGN KEY (`createdBy`) REFERENCES `users`(`id`) ON DELETE SET NULL
);

-- Dữ liệu mẫu chatGroups
INSERT INTO `chatGroups` (`groupName`, `createdBy`, `createdAt`) VALUES
                                                                     ('Gia Đình', 1, 1775463100000),
                                                                     ('Team Android', 1, 1775463200000),
                                                                     ('Lớp Đại Học', 5, 1775463300000);

-- =============================================
-- 5. BẢNG GROUP MEMBERS
-- =============================================
CREATE TABLE `groupMembers` (
                                `id` INT AUTO_INCREMENT PRIMARY KEY,
                                `groupId` INT NOT NULL,
                                `userId` INT NOT NULL,
                                CONSTRAINT `fkMemberGroup` FOREIGN KEY (`groupId`) REFERENCES `chatGroups`(`id`) ON DELETE CASCADE,
                                CONSTRAINT `fkMemberUser` FOREIGN KEY (`userId`) REFERENCES `users`(`id`) ON DELETE CASCADE,
                                UNIQUE (`groupId`, `userId`)
);

-- Dữ liệu mẫu groupMembers
INSERT INTO `groupMembers` (`groupId`, `userId`) VALUES
                                                     (1, 1), (1, 2), (1, 3), (1, 8), (1, 10),
                                                     (2, 1), (2, 4), (2, 5), (2, 6), (2, 7),
                                                     (3, 5), (3, 6), (3, 7), (3, 9);

-- =============================================
-- 6. BẢNG MESSAGES
-- =============================================
CREATE TABLE `messages` (
                            `id` INT AUTO_INCREMENT PRIMARY KEY,
                            `senderId` INT NOT NULL,
                            `receiverId` INT NULL,
                            `groupId` INT NULL,
                            `content` TEXT,
                            `messageType` VARCHAR(20) DEFAULT 'TEXT',
                            `status` VARCHAR(20) DEFAULT 'SENT',
                            `createdAt` BIGINT,
                            CONSTRAINT `fkMessageSender` FOREIGN KEY (`senderId`) REFERENCES `users`(`id`) ON DELETE CASCADE,
                            CONSTRAINT `fkMessageReceiver` FOREIGN KEY (`receiverId`) REFERENCES `users`(`id`) ON DELETE CASCADE,
                            CONSTRAINT `fkMessageGroup` FOREIGN KEY (`groupId`) REFERENCES `chatGroups`(`id`) ON DELETE CASCADE
);

-- Dữ liệu mẫu messages (có cả chat 1-1 và group)
INSERT INTO `messages` (`senderId`, `receiverId`, `groupId`, `content`, `messageType`, `status`, `createdAt`) VALUES
-- Chat 1-1 với Alice
(1, 2, NULL, 'Chào Alice, hôm nay em khỏe không?', 'TEXT', 'READ', 1775463600000),
(2, 1, NULL, 'Chào anh Loopy, em khỏe lắm ạ!', 'TEXT', 'READ', 1775463660000),
(1, 2, NULL, 'hjhj', 'TEXT', 'READ', 1775465220000),

-- Chat 1-1 với Mark
(1, 3, NULL, 'Tối nay đi làm tí cafe không?', 'TEXT', 'READ', 1775464140000),
(3, 1, NULL, 'Ok luôn, quán cũ nhé.', 'TEXT', 'SENT', 1775464200000),

-- Chat nhóm Gia Đình (groupId = 1)
(1, NULL, 1, 'Cả nhà ăn cơm chưa?', 'TEXT', 'READ', 1775465400000),
(2, NULL, 1, 'Con ăn rồi ạ!', 'TEXT', 'READ', 1775465460000);

-- =============================================
-- 7. INDEX TỐI ƯU
-- =============================================
CREATE INDEX idxMessageSender ON messages(senderId);
CREATE INDEX idxMessageReceiver ON messages(receiverId);
CREATE INDEX idxMessageGroup ON messages(groupId);
CREATE INDEX idxFriendshipUser1 ON friendships(user1Id);
CREATE INDEX idxFriendshipUser2 ON friendships(user2Id);
CREATE INDEX idxFriendshipStatus ON friendships(status);
CREATE INDEX idxGroupCreatedBy ON chatGroups(createdBy);