package com.bili.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 评论实体类 (对应 v_comment 表)
 * id: 用户id(发布者,保留原字段)
 * v_id: 视频id(优化后新增,关联 video.v_id)
 * cid:  评论主键(优化后新增)
 */
@Data
@TableName("v_comment")
public class VComment {

    /** 评论id(主键,自增) */
    @TableId(type = IdType.AUTO)
    private Integer cid;

    /** 用户id(发布评论的用户) */
    private String id;

    /**
     * 视频id
     * 关键: 字段名 vId(小写v+大写I) 会导致 Lombok 生成的 getVId() 被 Jackson 误推断为 vid
     * 需要同时在字段、getter、setter 上加 @JsonProperty("vId") 让 Jackson 合并为同一个属性
     */
    @Getter(onMethod_ = @JsonProperty("vId"))
    @Setter(onMethod_ = @JsonProperty("vId"))
    @JsonProperty("vId")
    private String vId;

    /** 评论数量(冗余统计字段) */
    private Integer commentNum;

    /** 评论内容 */
    private String commentContent;

    /** 评论时间(原字段名保留) */
    private LocalDateTime commentFrame;

    /** 评论点赞数 */
    private Integer likeNum;
}
