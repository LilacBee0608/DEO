/*
 Navicat Premium Dump SQL

 Source Server         : MySQL
 Source Server Type    : MySQL
 Source Server Version : 80028 (8.0.28)
 Source Host           : localhost:3306
 Source Schema         : deo

 Target Server Type    : MySQL
 Target Server Version : 80028 (8.0.28)
 File Encoding         : 65001

 Date: 25/08/2026 13:00:08
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for danmu
-- ----------------------------
DROP TABLE IF EXISTS `danmu`;
CREATE TABLE `danmu`  (
  `did` int NOT NULL AUTO_INCREMENT COMMENT '弹幕id',
  `id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户id(发布弹幕的用户)',
  `v_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '视频id(弹幕所属视频)',
  `danmu_num` int NULL DEFAULT 0 COMMENT '弹幕数量',
  `danmu_content` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '弹幕内容',
  `danmu_frame` int NULL DEFAULT NULL COMMENT '弹幕出现时间(视频时间轴秒数)',
  `color` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '#FFFFFF' COMMENT '弹幕颜色',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`did`) USING BTREE,
  INDEX `idx_video`(`v_id` ASC) USING BTREE,
  INDEX `idx_frame`(`v_id` ASC, `danmu_frame` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '弹幕表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of danmu
-- ----------------------------
INSERT INTO `danmu` VALUES (1, '1', 'v001', 0, '111', 5, '#FFFFFF', '2026-08-20 16:58:36');
INSERT INTO `danmu` VALUES (2, '2', 'v001', 0, '222', 12, '#FF60C2', '2026-08-20 16:58:36');
INSERT INTO `danmu` VALUES (3, '1', 'v001', 0, '333', 20, '#FFCC00', '2026-08-20 16:58:36');
INSERT INTO `danmu` VALUES (4, '2', 'v002', 0, '444', 8, '#00BFFF', '2026-08-20 16:58:36');
INSERT INTO `danmu` VALUES (5, '3', 'v003', 0, '555', 15, '#FF60C2', '2026-08-20 16:58:36');
INSERT INTO `danmu` VALUES (6, '1', 'v004', 0, '666!', 10, '#FF0000', '2026-08-20 16:58:36');
INSERT INTO `danmu` VALUES (7, '4', 'v004', 0, 'test', 17, '#FFFFFF', '2026-08-20 17:02:04');
INSERT INTO `danmu` VALUES (8, '1', 'v004', 0, '11111', 9, '#FFFFFF', '2026-08-21 09:34:04');
INSERT INTO `danmu` VALUES (9, '1', 'v004', 0, 'test', 4, '#ff0000', '2026-08-25 11:07:45');

-- ----------------------------
-- Table structure for history
-- ----------------------------
DROP TABLE IF EXISTS `history`;
CREATE TABLE `history`  (
  `hid` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `v_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `watch_time` datetime NOT NULL,
  PRIMARY KEY (`hid`) USING BTREE,
  UNIQUE INDEX `uk_user_video`(`user_id` ASC, `v_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of history
-- ----------------------------
INSERT INTO `history` VALUES (1, 1, 'v004', '2026-08-25 11:21:05');
INSERT INTO `history` VALUES (2, 1, 'v001', '2026-08-25 10:55:20');
INSERT INTO `history` VALUES (3, 1, 'v003', '2026-08-24 17:23:11');
INSERT INTO `history` VALUES (4, 1, 'v002', '2026-08-24 17:23:12');
INSERT INTO `history` VALUES (19, 1, 'v89f7d602', '2026-08-25 11:28:36');

-- ----------------------------
-- Table structure for v_comment
-- ----------------------------
DROP TABLE IF EXISTS `v_comment`;
CREATE TABLE `v_comment`  (
  `cid` int NOT NULL AUTO_INCREMENT COMMENT '评论id',
  `id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户id(发布评论的用户)',
  `v_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '视频id(评论所属视频)',
  `comment_num` int NULL DEFAULT 0 COMMENT '评论数量',
  `comment_content` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评论内容',
  `comment_frame` datetime NULL DEFAULT NULL COMMENT '评论时间',
  `like_num` int NULL DEFAULT 0 COMMENT '评论点赞数',
  PRIMARY KEY (`cid`) USING BTREE,
  INDEX `idx_video`(`v_id` ASC) USING BTREE,
  INDEX `idx_time`(`v_id` ASC, `comment_frame` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of v_comment
-- ----------------------------
INSERT INTO `v_comment` VALUES (1, '1', 'v001', 0, '已收藏', '2026-08-19 10:00:00', 8);
INSERT INTO `v_comment` VALUES (2, '2', 'v001', 0, '期待更新下一集', '2026-08-19 11:30:00', 5);
INSERT INTO `v_comment` VALUES (3, '3', 'v002', 0, '确实好用', '2026-08-19 14:00:00', 16);
INSERT INTO `v_comment` VALUES (4, '4', 'v004', 0, '123', '2026-08-20 16:58:43', 25);
INSERT INTO `v_comment` VALUES (5, '4', 'v004', 0, '111', '2026-08-20 17:06:06', 0);
INSERT INTO `v_comment` VALUES (6, '5', 'v004', 0, '1111', '2026-08-20 17:06:48', 25);

-- ----------------------------
-- Table structure for v_favorite
-- ----------------------------
DROP TABLE IF EXISTS `v_favorite`;
CREATE TABLE `v_favorite`  (
  `id` int NOT NULL COMMENT '用户id',
  `v_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '视频id',
  PRIMARY KEY (`id`, `v_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '视频收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of v_favorite
-- ----------------------------
INSERT INTO `v_favorite` VALUES (1, 'v001');
INSERT INTO `v_favorite` VALUES (1, 'v004');
INSERT INTO `v_favorite` VALUES (2, 'v001');
INSERT INTO `v_favorite` VALUES (3, 'v003');

-- ----------------------------
-- Table structure for v_like
-- ----------------------------
DROP TABLE IF EXISTS `v_like`;
CREATE TABLE `v_like`  (
  `id` int NOT NULL COMMENT '用户id',
  `v_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '视频id',
  PRIMARY KEY (`id`, `v_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '视频点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of v_like
-- ----------------------------
INSERT INTO `v_like` VALUES (1, 'v001');
INSERT INTO `v_like` VALUES (1, 'v002');
INSERT INTO `v_like` VALUES (1, 'v004');
INSERT INTO `v_like` VALUES (1, 'va8ce6c9c');
INSERT INTO `v_like` VALUES (2, 'v001');
INSERT INTO `v_like` VALUES (3, 'v001');

-- ----------------------------
-- Table structure for v_user
-- ----------------------------
DROP TABLE IF EXISTS `v_user`;
CREATE TABLE `v_user`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `user_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户昵称',
  `user_pswd` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户密码',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of v_user
-- ----------------------------
INSERT INTO `v_user` VALUES (1, 'admin', '123456');
INSERT INTO `v_user` VALUES (2, '张三', '123456');
INSERT INTO `v_user` VALUES (3, '李四', '123456');
INSERT INTO `v_user` VALUES (4, 'testuser2026', '123456');
INSERT INTO `v_user` VALUES (5, '11111111', '111111');

-- ----------------------------
-- Table structure for video
-- ----------------------------
DROP TABLE IF EXISTS `video`;
CREATE TABLE `video`  (
  `v_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '视频id',
  `id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '作者id',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标题',
  `tags` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签',
  `play_num` int NULL DEFAULT 0 COMMENT '播放量',
  `like_num` int NULL DEFAULT 0 COMMENT '点赞量',
  `share_num` int NULL DEFAULT 0 COMMENT '分享量',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '简介',
  `cover_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '封面URL',
  `video_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '视频URL',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`v_id`) USING BTREE,
  INDEX `idx_author`(`id` ASC) USING BTREE,
  INDEX `idx_tags`(`tags` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '视频表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of video
-- ----------------------------
INSERT INTO `video` VALUES ('v001', '1', '氰化物欢乐秀', '动漫', 1033, 257, 32, '最爱的小剧场', '/api/files/covers/1.jpg', '/api/files/videos/1-1.mp4\r\n/api/files/videos/1-2.mp4', '2026-08-20 16:58:36');
INSERT INTO `video` VALUES ('v002', '1', '太刀侠永远的梦想', '游戏', 2053, 512, 64, '原来你也玩怪猎', '/api/files/covers/2.jpg', '/api/files/videos/2.mp4', '2026-08-20 16:58:36');
INSERT INTO `video` VALUES ('v003', '2', '”电子海洋“复刻教程来啦！', '编程', 5128, 1024, 128, '可以用嘉立创免费打板', '/api/files/covers/3.jpg', '/api/files/videos/3.mp4', '2026-08-20 16:58:36');
INSERT INTO `video` VALUES ('v004', '3', '[4K]Rick Roll', '音乐', 8243, 2049, 256, '你又被骗了', '/api/files/covers/4.jpg', '/api/files/videos/4.mp4', '2026-08-20 16:58:36');
INSERT INTO `video` VALUES ('va8ce6c9c', '1', '𝑩𝒍𝒐𝒐𝒅 𝑰𝒏 𝑻𝒉𝒆 𝑾𝒊𝒏𝒆 _ 烹肉为实，酿血为酒', '美食', 7, 1, 0, '男女通吃', '/api/files/covers/5.jpg', '/api/files/videos/5.mp4', '2026-08-21 09:38:33');

SET FOREIGN_KEY_CHECKS = 1;
