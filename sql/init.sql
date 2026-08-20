-- ============================================================
-- 仿哔哩哔哩视频弹幕互动网站 - 数据库初始化脚本
-- 数据库名: deo
-- 说明: 在用户原始SQL基础上优化
--       1) 保留用户原有字段(命名与含义不变)
--       2) 为弹幕/评论表补充独立主键与视频外键,支撑完整功能
--       3) 增加必要的索引,提升查询性能
-- ============================================================

DROP DATABASE IF EXISTS deo;
CREATE DATABASE deo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE deo;

-- ------------------------------------------------------------
-- 1. 用户表 v_user (保留原设计)
-- ------------------------------------------------------------
CREATE TABLE v_user (
    id        INT          NOT NULL PRIMARY KEY             COMMENT '用户id',
    user_name VARCHAR(20)                                  COMMENT '用户昵称',
    user_pswd VARCHAR(50)                                  COMMENT '用户密码(加密后存储)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ------------------------------------------------------------
-- 2. 视频表 video (保留原设计)
--    说明: id 字段为作者id,与 v_user.id 关联
-- ------------------------------------------------------------
CREATE TABLE video (
    v_id        VARCHAR(10) PRIMARY KEY                    COMMENT '视频id',
    id          VARCHAR(20)                                COMMENT '作者id',
    title       VARCHAR(100)                               COMMENT '标题',
    tags        VARCHAR(10)                                COMMENT '标签',
    play_num    INT DEFAULT 0                              COMMENT '播放量',
    like_num    INT DEFAULT 0                              COMMENT '点赞量',
    share_num   INT DEFAULT 0                              COMMENT '分享量',
    description TEXT                                       COMMENT '简介',
    cover_url   VARCHAR(255)                               COMMENT '封面URL',
    video_url   VARCHAR(255)                               COMMENT '视频URL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP         COMMENT '创建时间',
    INDEX idx_author (id),
    INDEX idx_tags (tags)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频表';

-- ------------------------------------------------------------
-- 3. 弹幕表 danmu (在原基础上优化)
--    原字段: id(用户id)、danmu_num、danmu_content、danmu_frame
--    优化: 增加 did 主键(独立弹幕id)与 v_id 视频外键
--          原字段 id 保留为用户id(发布者)
-- ------------------------------------------------------------
CREATE TABLE danmu (
    did           INT AUTO_INCREMENT PRIMARY KEY            COMMENT '弹幕id(主键,自增)',
    id            VARCHAR(20)                              COMMENT '用户id(发布弹幕的用户)',
    v_id          VARCHAR(10)                              COMMENT '视频id(弹幕所属视频)',
    danmu_num     INT DEFAULT 0                           COMMENT '弹幕数量(冗余统计字段,保留)',
    danmu_content VARCHAR(100)                            COMMENT '弹幕内容',
    danmu_frame   INT                                     COMMENT '弹幕出现时间(视频时间轴秒数)',
    color         VARCHAR(10) DEFAULT '#FFFFFF'           COMMENT '弹幕颜色',
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP       COMMENT '发送时间',
    INDEX idx_video (v_id),
    INDEX idx_frame (v_id, danmu_frame)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='弹幕表';

-- ------------------------------------------------------------
-- 4. 评论表 v_comment (在原基础上优化)
--    原字段: id(用户id)、comment_num、comment_content、comment_frame(评论时间)
--    优化: 增加 cid 主键(独立评论id)与 v_id 视频外键
--          将 comment_frame 改名为 create_time 更直观(DATETIME 类型,与原意一致)
--          原字段 id 保留为用户id(发布者)
-- ------------------------------------------------------------
CREATE TABLE v_comment (
    cid             INT AUTO_INCREMENT PRIMARY KEY         COMMENT '评论id(主键,自增)',
    id              VARCHAR(20)                           COMMENT '用户id(发布评论的用户)',
    v_id            VARCHAR(10)                           COMMENT '视频id(评论所属视频)',
    comment_num     INT DEFAULT 0                         COMMENT '评论数量(冗余统计字段,保留)',
    comment_content VARCHAR(100)                         COMMENT '评论内容',
    comment_frame   DATETIME                             COMMENT '评论时间(原字段名保留)',
    like_num        INT DEFAULT 0                         COMMENT '评论点赞数',
    INDEX idx_video (v_id),
    INDEX idx_time (v_id, comment_frame)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- ------------------------------------------------------------
-- 5. 视频点赞表 v_like (新增,支撑点赞功能)
-- ------------------------------------------------------------
CREATE TABLE v_like (
    id      INT NOT NULL                                  COMMENT '用户id',
    v_id    VARCHAR(10)                                  COMMENT '视频id',
    PRIMARY KEY (id, v_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频点赞表';

-- ------------------------------------------------------------
-- 6. 视频收藏表 v_favorite (新增,支撑收藏功能)
-- ------------------------------------------------------------
CREATE TABLE v_favorite (
    id      INT NOT NULL                                  COMMENT '用户id',
    v_id    VARCHAR(10)                                  COMMENT '视频id',
    PRIMARY KEY (id, v_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频收藏表';

-- ------------------------------------------------------------
-- 初始化测试数据
-- ------------------------------------------------------------

-- 用户数据 (密码明文为 123456,生产环境应加密)
INSERT INTO v_user (id, user_name, user_pswd) VALUES
    (1, 'admin',    '123456'),
    (2, '张三',     '123456'),
    (3, '李四',     '123456');

-- 视频数据
INSERT INTO video (v_id, id, title, tags, play_num, like_num, share_num, description, cover_url, video_url) VALUES
    ('v001', '1', 'SpringBoot 3 入门教程', '编程', 1024, 256, 32, '从零开始学习SpringBoot 3', 'https://via.placeholder.com/300x200', 'https://www.w3schools.com/html/mov_bbb.mp4'),
    ('v002', '1', 'Vue3 组合式API详解', '编程', 2048, 512, 64, '深入理解Vue3的Composition API', 'https://via.placeholder.com/300x200', 'https://www.w3schools.com/html/movie.mp4'),
    ('v003', '2', '日常vlog 美食探店', '生活', 5120, 1024, 128, '探店美食vlog分享', 'https://via.placeholder.com/300x200', 'https://www.w3schools.com/html/mov_bbb.mp4'),
    ('v004', '3', '动漫剪辑 高燃瞬间', '动漫', 8192, 2048, 256, '精选动漫高燃片段', 'https://via.placeholder.com/300x200', 'https://www.w3schools.com/html/movie.mp4');

-- 弹幕数据
INSERT INTO danmu (id, v_id, danmu_content, danmu_frame, color) VALUES
    ('1', 'v001', '老师讲得很清楚', 5,  '#FFFFFF'),
    ('2', 'v001', '这个知识点很重要', 12, '#FF60C2'),
    ('1', 'v001', '已三连', 20, '#FFCC00'),
    ('2', 'v002', 'Vue3真香', 8,  '#00BFFF'),
    ('3', 'v003', '看着好饿', 15, '#FF60C2'),
    ('1', 'v004', '高燃!', 10, '#FF0000');

-- 评论数据
INSERT INTO v_comment (id, v_id, comment_content, comment_frame, like_num) VALUES
    ('1', 'v001', '干货满满,已收藏', '2026-08-19 10:00:00', 8),
    ('2', 'v001', '期待更新下一集', '2026-08-19 11:30:00', 5),
    ('3', 'v002', 'Composition API确实好用', '2026-08-19 14:00:00', 16);

-- 点赞数据
INSERT INTO v_like (id, v_id) VALUES
    (2, 'v001'),
    (3, 'v001'),
    (1, 'v002');

-- 收藏数据
INSERT INTO v_favorite (id, v_id) VALUES
    (2, 'v001'),
    (3, 'v003');
