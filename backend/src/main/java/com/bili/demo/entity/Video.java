package com.bili.demo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 视频实体类 (对应 video 表)
 * 字段与 v_user.id 关联(作者id)
 */
@Data
@TableName("video")
public class Video {

    /**
     * 视频id(主键,字符串)
     * 字段名 vId 会导致 Lombok 生成的 getVId() 被 Jackson 误推断为 vid
     * 需要同时在字段、getter、setter 上加 @JsonProperty("vId") 让 Jackson 合并为同一个属性
     */
    @TableId
    @Getter(onMethod_ = @JsonProperty("vId"))
    @Setter(onMethod_ = @JsonProperty("vId"))
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

    /**
     * 作者昵称(非数据库字段)
     * 由 Mapper 的 SQL LEFT JOIN v_user 查询填充,用于列表展示
     */
    @TableField(exist = false)
    private String authorName;
}
