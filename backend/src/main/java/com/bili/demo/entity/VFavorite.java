package com.bili.demo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 视频收藏实体类 (对应 v_favorite 表)
 * 联合主键 (id, v_id): 一个用户对一个视频只能收藏一次
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("v_favorite")
public class VFavorite {
    /** 用户id */
    private Integer id;
    /** 视频id */
    @JsonProperty("vId")
    private String vId;
}
