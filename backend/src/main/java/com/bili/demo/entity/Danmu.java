package com.bili.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 弹幕实体类 (对应 danmu 表)
 * id: 用户id(发布者,保留原字段)
 * v_id: 视频id(优化后新增,关联 video.v_id)
 * did:  弹幕主键(优化后新增)
 */
@Data
@TableName("danmu")
public class Danmu {

    /** 弹幕id(主键,自增) */
    @TableId(type = IdType.AUTO)
    private Integer did;

    /** 用户id(发布弹幕的用户) */
    private String id;

    /** 视频id */
    @JsonProperty("vId")
    private String vId;

    /** 弹幕数量(冗余统计字段) */
    private Integer danmuNum;

    /** 弹幕内容 */
    private String danmuContent;

    /** 弹幕出现时间(视频时间轴秒数) */
    private Integer danmuFrame;

    /** 弹幕颜色(默认白色) */
    private String color;

    /** 发送时间 */
    private LocalDateTime createTime;
}
