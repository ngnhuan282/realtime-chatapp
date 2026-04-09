-- =============================================
-- 1. KHỞI TẠO DATABASE
-- =============================================
DROP DATABASE IF EXISTS chat_app;
CREATE DATABASE chat_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE chat_app;

-- =============================================
-- 2. BẢNG USERS [cite: 10, 215]
-- =============================================
CREATE TABLE `users` (
                         `id` INT AUTO_INCREMENT PRIMARY KEY,
                         `username` VARCHAR(50) NOT NULL UNIQUE,
                         `password` VARCHAR(255) NOT NULL,
                         `displayName` VARCHAR(100),
                         `phoneNumber` VARCHAR(20) UNIQUE,
                         `avatar` VARCHAR(255),
                         `createdAt` BIGINT
);

-- Dữ liệu mẫu users
INSERT INTO `users` (`username`, `password`, `displayName`, `phoneNumber`, `avatar`, `createdAt`) VALUES
                                                                                                      ('loopy', '123456', 'Loopy Cute', '0912345678', NULL, 1775463000000),
                                                                                                      ('alice', '123456', 'Alice Green', '0987654321', NULL, 1775463000000),
                                                                                                      ('mark', '123456', 'Mark Rivers', '0978123456', NULL, 1775463000000),
                                                                                                      ('sarah', '123456', 'Sarah Woods', '0933456789', NULL, 1775463000000),
                                                                                                      ('david', '123456', 'David Chen', '0901122334', NULL, 1775463000000),
                                                                                                      ('emily', '123456', 'Emily Rose', '0945566778', NULL, 1775463000000),
                                                                                                      ('james', '123456', 'James Lee', '0919988776', NULL, 1775463000000),
                                                                                                      ('anna', '123456', 'Anna Smith', '0988776655', NULL, 1775463000000),
                                                                                                      ('johnny', '123456', 'Johnny Depp', '0933221100', NULL, 1775463000000),
                                                                                                      ('lisa', '123456', 'Lisa Blackpink', '0966554433', NULL, 1775463000000);

-- =============================================
-- 3. BẢNG FRIENDSHIPS (Có cột Status)
-- =============================================
CREATE TABLE `friendships` (
                               `id` INT AUTO_INCREMENT PRIMARY KEY,
                               `user1Id` INT NOT NULL, -- Người gửi lời mời
                               `user2Id` INT NOT NULL, -- Người nhận lời mời
                               `status` VARCHAR(20) DEFAULT 'PENDING', -- PENDING (Chờ), ACCEPTED (Bạn bè)
                               `createdAt` BIGINT,
                               UNIQUE KEY `uk_friendship_pair` (`user1Id`, `user2Id`),
                               CONSTRAINT `fk_friendship_user1` FOREIGN KEY (`user1Id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
                               CONSTRAINT `fk_friendship_user2` FOREIGN KEY (`user2Id`) REFERENCES `users`(`id`) ON DELETE CASCADE
);

-- Dữ liệu mẫu friendships
-- Status 'ACCEPTED': Đã là bạn, sẽ hiện "Bạn bè" ở màn hình tìm kiếm
-- Status 'PENDING': Đang chờ, người nhận (user2Id) có thể nhấn Chấp nhận
INSERT INTO `friendships` (`user1Id`, `user2Id`, `status`, `createdAt`) VALUES
                                                                            (1, 2, 'ACCEPTED', 1775463000000), -- loopy & alice (Bạn bè)
                                                                            (1, 3, 'ACCEPTED', 1775463000000), -- loopy & mark (Bạn bè)
                                                                            (4, 1, 'PENDING', 1775463000000),  -- sarah gửi lời mời cho loopy (Đang chờ)
                                                                            (5, 1, 'PENDING', 1775463000000),  -- david gửi lời mời cho loopy (Đang chờ)
                                                                            (1, 6, 'ACCEPTED', 1775463000000), -- loopy & emily (Bạn bè)
                                                                            (2, 5, 'ACCEPTED', 1775463000000),
                                                                            (8, 2, 'PENDING', 1775463000000),  -- anna gửi cho alice
                                                                            (3, 7, 'ACCEPTED', 1775463000000);

-- =============================================
-- 4. BẢNG CHAT GROUPS [cite: 211, 217]
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
-- 5. BẢNG GROUP MEMBERS [cite: 212, 218]
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
                                                     (1, 1), (1, 2), (1, 3), (1, 8), (1, 10),
                                                     (2, 1), (2, 4), (2, 5), (2, 6), (2, 7),
                                                     (3, 5), (3, 6), (3, 7), (3, 9);

-- =============================================
-- 6. BẢNG MESSAGES [cite: 210, 216]
-- =============================================
CREATE TABLE `messages` (
                            `id` INT AUTO_INCREMENT PRIMARY KEY,
                            `senderId` INT NOT NULL,
                            `receiverId` INT NULL,
                            `groupId` INT NULL,
                            `content` TEXT,
                            `messageType` VARCHAR(20) DEFAULT 'TEXT',
                            `status` VARCHAR(20) DEFAULT 'SENT', -- SENT, DELIVERED, READ
                            `createdAt` BIGINT,
                            CONSTRAINT `fkMessageSender` FOREIGN KEY (`senderId`) REFERENCES `users`(`id`) ON DELETE CASCADE,
                            CONSTRAINT `fkMessageReceiver` FOREIGN KEY (`receiverId`) REFERENCES `users`(`id`) ON DELETE CASCADE,
                            CONSTRAINT `fkMessageGroup` FOREIGN KEY (`groupId`) REFERENCES `chatGroups`(`id`) ON DELETE CASCADE
);

INSERT INTO `messages` (`senderId`, `receiverId`, `groupId`, `content`, `messageType`, `status`, `createdAt`) VALUES
                                                                                                                  (1, 2, NULL, 'Chào Alice, hôm nay em khỏe không?', 'TEXT', 'READ', 1775463600000),
                                                                                                                  (2, 1, NULL, 'Chào anh Loopy, em khỏe lắm ạ!', 'TEXT', 'READ', 1775463660000),
                                                                                                                  (1, NULL, 1, 'Cả nhà ăn cơm chưa?', 'TEXT', 'READ', 1775465400000);

-- =============================================
-- 7. INDEX TỐI ƯU
-- =============================================
CREATE INDEX idxMessageSender ON messages(senderId);
CREATE INDEX idxMessageReceiver ON messages(receiverId);
CREATE INDEX idxMessageGroup ON messages(groupId);
CREATE INDEX idxFriendshipUser1 ON friendships(user1Id);
CREATE INDEX idxFriendshipUser2 ON friendships(user2Id);
CREATE INDEX idxFriendshipStatus ON friendships(status);