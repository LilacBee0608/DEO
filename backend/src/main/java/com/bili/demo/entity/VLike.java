package com.bili.demo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 视频点赞实体类 (对应 v_like 表)
 * 联合主键 (id, v_id): 一个用户对一个视频只能点赞一次
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("v_like")
public class VLike {
    /** 用户id */
    private Integer id;

    /**
     * 视频id
     * 字段名 vId 会导致 Lombok 生成的 getVId() 被 Jackson 误推断为 vid
     * 需要同时在字段、getter、setter 上加 @JsonProperty("vId") 让 Jackson 合并为同一个属性
     */
    @Getter(onMethod_ = @JsonProperty("vId"))
    @Setter(onMethod_ = @JsonProperty("vId"))
    @JsonProperty("vId")
    private String vId;
}
