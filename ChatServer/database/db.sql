-- =============================================
-- 1. KHOI TAO DATABASE
-- =============================================
DROP DATABASE IF EXISTS chat_app;
CREATE DATABASE chat_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE chat_app;

-- =============================================
-- 2. BANG USERS
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

-- Du lieu mau users
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
-- 3. BANG FRIENDSHIPS
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

-- Du lieu mau friendships (Da loai bo quan he giua Loopy va Sarah)
INSERT INTO `friendships` (`user1Id`, `user2Id`, `status`, `createdAt`) VALUES
                                                                            (1, 2, 'ACCEPTED', 1775463000000),   -- Loopy - Alice
                                                                            (1, 3, 'ACCEPTED', 1775463000000),   -- Loopy - Mark
                                                                            (1, 6, 'ACCEPTED', 1775463000000),   -- Loopy - Emily
                                                                            (2, 5, 'ACCEPTED', 1775463000000),   -- Alice - David
                                                                            (3, 7, 'ACCEPTED', 1775463000000),   -- Mark - James
                                                                            (8, 2, 'PENDING', 1775463000000),    -- Anna - Alice
                                                                            (5, 1, 'PENDING', 1775463000000);    -- David - Loopy

-- =============================================
-- 4. BANG CHAT GROUPS
-- =============================================
CREATE TABLE `chatGroups` (
                              `id` INT AUTO_INCREMENT PRIMARY KEY,
                              `groupName` VARCHAR(100),
                              `createdBy` INT,
                              `createdAt` BIGINT,
                              CONSTRAINT `fkGroupCreator` FOREIGN KEY (`createdBy`) REFERENCES `users`(`id`) ON DELETE SET NULL
);

-- Du lieu mau chatGroups
INSERT INTO `chatGroups` (`groupName`, `createdBy`, `createdAt`) VALUES
                                                                     ('Gia Dinh', 1, 1775463100000),
                                                                     ('Team Android', 1, 1775463200000),
                                                                     ('Lop Dai Hoc', 5, 1775463300000);

-- =============================================
-- 5. BANG GROUP MEMBERS
-- =============================================
CREATE TABLE `groupMembers` (
                                `id` INT AUTO_INCREMENT PRIMARY KEY,
                                `groupId` INT NOT NULL,
                                `userId` INT NOT NULL,
                                CONSTRAINT `fkMemberGroup` FOREIGN KEY (`groupId`) REFERENCES `chatGroups`(`id`) ON DELETE CASCADE,
                                CONSTRAINT `fkMemberUser` FOREIGN KEY (`userId`) REFERENCES `users`(`id`) ON DELETE CASCADE,
                                UNIQUE (`groupId`, `userId`)
);

-- Du lieu mau groupMembers
INSERT INTO `groupMembers` (`groupId`, `userId`) VALUES
                                                     (1, 1), (1, 2), (1, 3), (1, 8), (1, 10),   -- Gia Dinh
                                                     (2, 1), (2, 4), (2, 5), (2, 6), (2, 7),   -- Team Android
                                                     (3, 5), (3, 6), (3, 7), (3, 9);           -- Lop Dai Hoc

-- =============================================
-- 6. BANG MESSAGES
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

-- Du lieu mau messages (khong dau)
INSERT INTO `messages` (`senderId`, `receiverId`, `groupId`, `content`, `messageType`, `status`, `createdAt`) VALUES
-- Chat 1-1 voi Alice
(1, 2, NULL, 'Chao Alice, hom nay em khoe khong?', 'TEXT', 'READ', 1775463600000),
(2, 1, NULL, 'Chao anh Loopy, em khoe lam a!', 'TEXT', 'READ', 1775463660000),
(1, 2, NULL, 'Haha vui qua!', 'TEXT', 'READ', 1775465220000),

-- Chat 1-1 voi Mark
(1, 3, NULL, 'Toi nay di lam ti ca phe khong?', 'TEXT', 'READ', 1775464140000),
(3, 1, NULL, 'Ok luon, quan cu nhe.', 'TEXT', 'SENT', 1775464200000),

-- Chat nhom Gia Dinh (groupId = 1)
(1, NULL, 1, 'Ca nha an com chua?', 'TEXT', 'READ', 1775465400000),
(2, NULL, 1, 'Con an roi a!', 'TEXT', 'READ', 1775465460000),
(1, NULL, 1, 'Moi nguoi an gi ngon the?', 'TEXT', 'READ', 1775465520000);

-- =============================================
-- 7. INDEX TOI UU
-- =============================================
CREATE INDEX idxMessageSender ON messages(senderId);
CREATE INDEX idxMessageReceiver ON messages(receiverId);
CREATE INDEX idxMessageGroup ON messages(groupId);
CREATE INDEX idxFriendshipUser1 ON friendships(user1Id);
CREATE INDEX idxFriendshipUser2 ON friendships(user2Id);
CREATE INDEX idxFriendshipStatus ON friendships(status);
CREATE INDEX idxGroupCreatedBy ON chatGroups(createdBy);