package com.bili.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 观看历史实体类 (对应 history 表)
 * 记录用户观看过的视频,联合唯一约束 (user_id, v_id) 保证不重复
 * 重复观看同一视频时更新 watch_time
 */
@Data
@TableName("history")
public class History {

    /** 记录id(主键,自增) */
    @TableId(type = IdType.AUTO)
    private Integer hid;

    /** 用户id(观看者) */
    private Integer userId;

    /**
     * 视频id
     * 字段名 vId(小写v+大写I) 需加 @JsonProperty 保证序列化输出为 "vId"
     * 只在字段上加一次即可,Jackson 会自动应用到 getter/setter
     */
    @JsonProperty("vId")
    private String vId;

    /** 观看时间(重复观看时更新为最新时间) */
    private LocalDateTime watchTime;
}
