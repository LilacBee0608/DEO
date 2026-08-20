package com.bili.demo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 视频实体类 (对应 video 表)
 * 字段与 v_user.id 关联(作者id)
 */
@Data
@TableName("video")
public class Video {

    /** 视频id(主键,字符串) */
    @TableId
    @JsonProperty("vId")
    private String vId;

    /** 作者id(关联 v_user.id) */
    private String id;

    /** 标题 */
    private String title;

    /** 标签 */
    private String tags;

    /** 播放量 */
    private Integer playNum;

    /** 点赞量 */
    private Integer likeNum;

    /** 分享量 */
    private Integer shareNum;

    /** 简介 */
    private String description;

    /** 封面URL */
    private String coverUrl;

    /** 视频URL */
    private String videoUrl;

    /** 创建时间 */
    private LocalDateTime createTime;
}
