/*
 Navicat MySQL Data Transfer

 Source Server         : ssmLearn
 Source Server Type    : MySQL
 Source Server Version : 50731
 Source Host           : localhost:3306
 Source Schema         : wallet

 Target Server Type    : MySQL
 Target Server Version : 50731
 File Encoding         : 65001

 Date: 27/11/2023 17:14:27
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for bankaccount
-- ----------------------------
DROP TABLE IF EXISTS `bankaccount`;
CREATE TABLE `bankaccount`  (
  `account_id` int(11) NOT NULL AUTO_INCREMENT,
  `bank_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `account_number` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `is_joint` tinyint(1) NOT NULL,
  PRIMARY KEY (`account_id`) USING BTREE,
  UNIQUE INDEX `account_number`(`account_number`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bankaccount
-- ----------------------------
INSERT INTO `bankaccount` VALUES (3, '2', '1237', 0);
INSERT INTO `bankaccount` VALUES (6, '2', '123', 0);
INSERT INTO `bankaccount` VALUES (7, '2', '1234', 1);
INSERT INTO `bankaccount` VALUES (8, '2', '123456789', 0);

-- ----------------------------
-- Table structure for deleteemail
-- ----------------------------
DROP TABLE IF EXISTS `deleteemail`;
CREATE TABLE `deleteemail`  (
  `email_id` int(255) NOT NULL,
  `email_address` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  PRIMARY KEY (`email_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of deleteemail
-- ----------------------------
INSERT INTO `deleteemail` VALUES (12, 'mail@qq.com');
INSERT INTO `deleteemail` VALUES (18, 'mail6@qq.com');

-- ----------------------------
-- Table structure for email
-- ----------------------------
DROP TABLE IF EXISTS `email`;
CREATE TABLE `email`  (
  `email_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NULL DEFAULT NULL,
  `email_address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `is_registered` tinyint(1) NULL DEFAULT NULL,
  `is_email_verified` tinyint(1) NOT NULL,
  PRIMARY KEY (`email_id`) USING BTREE,
  UNIQUE INDEX `email_address`(`email_address`) USING BTREE,
  INDEX `user_id`(`user_id`) USING BTREE,
  CONSTRAINT `Email_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of email
-- ----------------------------
INSERT INTO `email` VALUES (1, 1, '1234@qq.com', 1, 1);
INSERT INTO `email` VALUES (2, 2, '12374@qq.com', 1, 1);
INSERT INTO `email` VALUES (3, 3, '123754@qq.com', 1, 1);
INSERT INTO `email` VALUES (4, 5, '123744@qq.com', 1, 1);
INSERT INTO `email` VALUES (5, 6, '1237445@qq.com', 1, 1);
INSERT INTO `email` VALUES (6, 8, '12374485@qq.com', 1, 1);
INSERT INTO `email` VALUES (7, 9, '', 1, 1);
INSERT INTO `email` VALUES (8, 10, 'mail', 1, 1);
INSERT INTO `email` VALUES (9, 12, '4153sdafasdf', 1, 1);
INSERT INTO `email` VALUES (13, 17, 'mail2@qq.com', 1, 1);
INSERT INTO `email` VALUES (15, 19, 'qaq', 1, 1);
INSERT INTO `email` VALUES (17, 12, 'mail5@qq.com', 1, 1);
INSERT INTO `email` VALUES (19, 12, 'mail@qq.com', 1, 1);
INSERT INTO `email` VALUES (20, 20, 'qaqaq', 1, 1);
INSERT INTO `email` VALUES (21, 21, 'wuwenhan@whu.edu.cn', 1, 1);

-- ----------------------------
-- Table structure for phonenumber
-- ----------------------------
DROP TABLE IF EXISTS `phonenumber`;
CREATE TABLE `phonenumber`  (
  `phone_number` varchar(15) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `user_id` int(11) NULL DEFAULT NULL,
  `is_registered` tinyint(1) NULL DEFAULT NULL,
  `is_phone_verified` tinyint(1) NOT NULL,
  PRIMARY KEY (`phone_number`) USING BTREE,
  INDEX `user_id`(`user_id`) USING BTREE,
  CONSTRAINT `PhoneNumber_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of phonenumber
-- ----------------------------
INSERT INTO `phonenumber` VALUES ('', 9, 1, 1);
INSERT INTO `phonenumber` VALUES ('1234', 1, 1, 1);
INSERT INTO `phonenumber` VALUES ('123445', 5, 1, 1);
INSERT INTO `phonenumber` VALUES ('1234456', 6, 1, 1);
INSERT INTO `phonenumber` VALUES ('1234486', 8, 1, 1);
INSERT INTO `phonenumber` VALUES ('12345', 2, 1, 1);
INSERT INTO `phonenumber` VALUES ('123457', 3, 1, 1);
INSERT INTO `phonenumber` VALUES ('150', 21, 1, 1);
INSERT INTO `phonenumber` VALUES ('303', 17, 1, 1);
INSERT INTO `phonenumber` VALUES ('3034', 20, 1, 1);
INSERT INTO `phonenumber` VALUES ('343', 19, 1, 1);
INSERT INTO `phonenumber` VALUES ('78945', 12, 1, 1);

-- ----------------------------
-- Table structure for request
-- ----------------------------
DROP TABLE IF EXISTS `request`;
CREATE TABLE `request`  (
  `request_id` int(11) NOT NULL AUTO_INCREMENT,
  `requester_user_id` int(11) NULL DEFAULT NULL,
  `total_amount` decimal(10, 2) NOT NULL,
  `memo` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `request_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`request_id`) USING BTREE,
  INDEX `requester_user_id`(`requester_user_id`) USING BTREE,
  CONSTRAINT `Request_ibfk_1` FOREIGN KEY (`requester_user_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of request
-- ----------------------------

-- ----------------------------
-- Table structure for requestcontribution
-- ----------------------------
DROP TABLE IF EXISTS `requestcontribution`;
CREATE TABLE `requestcontribution`  (
  `contribution_id` int(11) NOT NULL AUTO_INCREMENT,
  `request_id` int(11) NULL DEFAULT NULL,
  `sender_phone_number` varchar(15) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `sender_email_id` int(11) NULL DEFAULT NULL,
  `transaction_id` int(11) NULL DEFAULT NULL,
  `contribution_amount` decimal(10, 2) NOT NULL,
  `is_contributed` tinyint(1) NOT NULL,
  PRIMARY KEY (`contribution_id`) USING BTREE,
  INDEX `request_id`(`request_id`) USING BTREE,
  INDEX `sender_phone_number`(`sender_phone_number`) USING BTREE,
  INDEX `sender_email_id`(`sender_email_id`) USING BTREE,
  INDEX `transaction_id`(`transaction_id`) USING BTREE,
  CONSTRAINT `RequestContribution_ibfk_1` FOREIGN KEY (`request_id`) REFERENCES `request` (`request_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `RequestContribution_ibfk_2` FOREIGN KEY (`sender_phone_number`) REFERENCES `phonenumber` (`phone_number`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `RequestContribution_ibfk_3` FOREIGN KEY (`sender_email_id`) REFERENCES `email` (`email_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `RequestContribution_ibfk_4` FOREIGN KEY (`transaction_id`) REFERENCES `transaction` (`transaction_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of requestcontribution
-- ----------------------------

-- ----------------------------
-- Table structure for transaction
-- ----------------------------
DROP TABLE IF EXISTS `transaction`;
CREATE TABLE `transaction`  (
  `transaction_id` int(11) NOT NULL AUTO_INCREMENT,
  `sender_user_id` int(11) NULL DEFAULT NULL,
  `recipient_user_id` int(11) NULL DEFAULT NULL,
  `recipient_email_id` int(11) NULL DEFAULT NULL,
  `recipient_phone_number` varchar(15) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `amount` decimal(10, 2) NOT NULL,
  `memo` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `transaction_start_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `transction_finished_time` timestamp(0) NULL DEFAULT NULL,
  `is_cancelled` tinyint(1) NULL DEFAULT NULL,
  `cancelled_time` timestamp(0) NULL DEFAULT NULL,
  `cancelled_reason` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  PRIMARY KEY (`transaction_id`) USING BTREE,
  INDEX `sender_user_id`(`sender_user_id`) USING BTREE,
  INDEX `recipient_user_id`(`recipient_user_id`) USING BTREE,
  INDEX `recipient_email_id`(`recipient_email_id`) USING BTREE,
  INDEX `recipient_phone_number`(`recipient_phone_number`) USING BTREE,
  CONSTRAINT `Transaction_ibfk_1` FOREIGN KEY (`sender_user_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `Transaction_ibfk_2` FOREIGN KEY (`recipient_user_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 38 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of transaction
-- ----------------------------
INSERT INTO `transaction` VALUES (1, 1, 2, NULL, '12345', 1000.00, '测试一下', '2023-11-24 08:21:43', NULL, 0, NULL, NULL);
INSERT INTO `transaction` VALUES (2, 1, 2, NULL, '12345', 1000.00, '测试一下', '2023-11-24 08:22:58', NULL, 0, NULL, NULL);
INSERT INTO `transaction` VALUES (3, 1, 2, NULL, '12345', 1000.00, '测试一下', '2023-11-24 08:23:21', NULL, 0, NULL, NULL);
INSERT INTO `transaction` VALUES (4, 1, 2, NULL, '12345', 1000.00, '测试一下', '2023-11-24 08:26:58', NULL, 0, NULL, NULL);
INSERT INTO `transaction` VALUES (5, 1, 12, 12, NULL, 1000.00, '测试一下', '2023-11-24 08:28:20', NULL, 0, NULL, NULL);
INSERT INTO `transaction` VALUES (6, 1, 12, 12, NULL, 1000.00, '测试一下', '2023-11-24 08:28:25', NULL, 0, NULL, NULL);
INSERT INTO `transaction` VALUES (7, 1, 12, 12, NULL, 1000.00, '测试一下', '2023-11-24 08:29:16', NULL, 0, NULL, NULL);
INSERT INTO `transaction` VALUES (8, 1, 12, 12, NULL, 1000.00, '测试一下', '2023-11-24 08:51:59', NULL, 0, NULL, NULL);
INSERT INTO `transaction` VALUES (9, 1, 12, 12, NULL, 1000.00, '测试一下', '2023-11-24 09:06:09', NULL, 0, NULL, NULL);
INSERT INTO `transaction` VALUES (10, 1, 12, 12, NULL, 1000.00, '测试一下', '2023-11-24 09:07:16', NULL, 0, NULL, NULL);
INSERT INTO `transaction` VALUES (11, 1, 12, 12, NULL, 1000.00, '测试一下', '2023-11-24 09:12:24', NULL, 0, NULL, NULL);
INSERT INTO `transaction` VALUES (12, 1, 12, 12, NULL, 1000.00, '测试一下', '2023-11-24 09:14:41', NULL, 0, NULL, NULL);
INSERT INTO `transaction` VALUES (13, 1, 12, 12, NULL, 1000.00, '测试一下', '2023-11-24 09:23:44', NULL, 0, NULL, NULL);
INSERT INTO `transaction` VALUES (14, 1, 12, 12, NULL, 1000.00, '测试一下', '2023-11-24 09:29:54', '2023-11-24 09:29:55', 0, NULL, NULL);
INSERT INTO `transaction` VALUES (15, 1, 12, 12, NULL, 1000.00, '测试一下', '2023-11-24 09:38:50', '2023-11-24 09:38:51', 0, NULL, NULL);
INSERT INTO `transaction` VALUES (16, 1, 12, 12, NULL, 1000.00, '测试一下', '2023-11-24 09:40:53', '2023-11-24 09:40:54', 0, NULL, NULL);
INSERT INTO `transaction` VALUES (19, 1, 17, NULL, '303', 1000.00, '测试一下', '2023-11-24 12:13:47', '2023-11-24 12:38:33', 0, NULL, NULL);
INSERT INTO `transaction` VALUES (20, 1, 17, NULL, '303', 1000.00, '测试一下', '2023-11-24 12:14:09', '2023-11-24 12:38:33', 0, NULL, NULL);
INSERT INTO `transaction` VALUES (21, 1, 17, NULL, '303', 2000.00, '测试一下', '2023-11-24 12:14:15', '2023-11-24 12:38:33', 0, NULL, NULL);
INSERT INTO `transaction` VALUES (22, 1, 17, 13, NULL, 5000.00, '测试一下', '2023-11-24 12:14:33', '2023-11-24 12:38:33', 0, NULL, NULL);
INSERT INTO `transaction` VALUES (23, 1, 17, 13, NULL, 4000.00, '测试一下', '2023-11-24 12:14:37', '2023-11-24 12:38:33', 0, NULL, NULL);
INSERT INTO `transaction` VALUES (24, 12, 17, NULL, '303', 500.00, '测试2', '2023-11-24 14:21:46', '2023-11-24 14:21:46', 0, NULL, NULL);
INSERT INTO `transaction` VALUES (25, 12, 17, NULL, '303', 500.00, '测试2', '2023-11-24 14:21:50', '2023-11-24 14:21:50', 0, NULL, NULL);
INSERT INTO `transaction` VALUES (26, 12, 17, NULL, '303', 500.00, '测试2', '2023-11-24 14:21:53', '2023-11-24 14:21:53', 0, NULL, NULL);
INSERT INTO `transaction` VALUES (27, 12, 20, NULL, '3034', 500.00, '测试2', '2023-11-24 14:22:45', '2023-11-24 14:24:07', 0, NULL, NULL);
INSERT INTO `transaction` VALUES (28, 12, 20, NULL, '3034', 500.00, '测试2', '2023-11-24 14:22:47', '2023-11-24 14:24:07', 0, NULL, NULL);
INSERT INTO `transaction` VALUES (29, 12, 20, NULL, '3034', 500.00, '测试2', '2023-11-24 14:22:48', '2023-11-24 14:24:07', 0, NULL, NULL);
INSERT INTO `transaction` VALUES (30, 1, 21, 21, NULL, 20000.00, '测试2', '2023-11-24 14:25:40', '2023-11-24 14:26:18', 0, NULL, NULL);
INSERT INTO `transaction` VALUES (31, 1, 21, 21, NULL, 20000.00, '测试2', '2023-11-24 14:25:42', '2023-11-24 14:26:18', 0, NULL, NULL);
INSERT INTO `transaction` VALUES (32, 1, 21, 21, NULL, 20000.00, '测试2', '2023-11-24 14:25:44', '2023-11-24 14:26:18', 0, NULL, NULL);
INSERT INTO `transaction` VALUES (33, 1, 21, 21, NULL, 20000.00, '测试2', '2023-11-24 14:25:45', '2023-11-24 14:26:18', 0, NULL, NULL);
INSERT INTO `transaction` VALUES (35, 1, 21, 21, NULL, 20000.00, '测试一下', '2023-11-27 07:54:35', '2023-11-27 07:54:36', 1, '2023-11-27 07:54:45', NULL);
INSERT INTO `transaction` VALUES (36, 1, 21, 21, NULL, 20000.00, '', '2023-11-27 07:55:26', '2023-11-27 07:55:26', 1, '2023-11-27 07:55:47', NULL);
INSERT INTO `transaction` VALUES (37, 1, 21, 21, NULL, 20000.00, '测试2', '2023-11-27 07:57:47', '2023-11-27 07:57:48', 1, '2023-11-27 07:58:04', 'test');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `ssn` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `password` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `balance` decimal(10, 2) NOT NULL,
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `ssn`(`ssn`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'qiu', '1234', '123', 680000.00);
INSERT INTO `user` VALUES (2, 'qiu2', '123', '123', 4000.00);
INSERT INTO `user` VALUES (3, 'qiu3', '1232', '12322', 0.00);
INSERT INTO `user` VALUES (5, 'qiu4', '12345', '12344', 0.00);
INSERT INTO `user` VALUES (6, 'qiu5', '123456', '12344', 0.00);
INSERT INTO `user` VALUES (8, 'qiu5', '1234568', '12344', 0.00);
INSERT INTO `user` VALUES (9, '', '', '', 0.00);
INSERT INTO `user` VALUES (10, 'wwh', '0000', '123456', 0.00);
INSERT INTO `user` VALUES (12, 'name1', '12352rfa', '321321313', 6000.00);
INSERT INTO `user` VALUES (17, 'zhangsan', 'oiuiui', '12344', 14500.00);
INSERT INTO `user` VALUES (19, 'zs', 'oiuiuii', '12344', 0.00);
INSERT INTO `user` VALUES (20, 'zsa', 'asss', '12347', 1500.00);
INSERT INTO `user` VALUES (21, 'wwh', '343434', '12347', 100000.00);

-- ----------------------------
-- Table structure for userbankaccount
-- ----------------------------
DROP TABLE IF EXISTS `userbankaccount`;
CREATE TABLE `userbankaccount`  (
  `user_account_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `account_id` int(11) NOT NULL,
  `is_primary` tinyint(1) NOT NULL,
  `is_verified` tinyint(1) NOT NULL,
  PRIMARY KEY (`user_account_id`) USING BTREE,
  INDEX `user_id`(`user_id`) USING BTREE,
  INDEX `account_id`(`account_id`) USING BTREE,
  CONSTRAINT `UserBankAccount_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `UserBankAccount_ibfk_2` FOREIGN KEY (`account_id`) REFERENCES `bankaccount` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of userbankaccount
-- ----------------------------
INSERT INTO `userbankaccount` VALUES (2, 12, 3, 0, 1);
INSERT INTO `userbankaccount` VALUES (3, 12, 6, 1, 1);
INSERT INTO `userbankaccount` VALUES (5, 1, 7, 1, 1);
INSERT INTO `userbankaccount` VALUES (6, 2, 7, 1, 1);
INSERT INTO `userbankaccount` VALUES (8, 12, 8, 0, 1);

SET FOREIGN_KEY_CHECKS = 1;
