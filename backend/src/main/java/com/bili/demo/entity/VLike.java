package com.bili.demo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    /** 视频id */
    private String vId;
}
