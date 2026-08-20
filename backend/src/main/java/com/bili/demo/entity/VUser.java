package com.bili.demo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户实体类 (对应 v_user 表)
 * - @TableName: 指定数据库表名
 * - @TableId:   指定主键字段(id 默认主键,这里显式声明)
 * - 字段使用包装类型(Integer)允许 null,方便插入时由数据库自增
 */
@Data
@TableName("v_user")
public class VUser {

    /** 用户id(主键) */
    @TableId
    private Integer id;

    /** 用户昵称 */
    private String userName;

    /** 用户密码(加密后存储,这里为简化实训,暂用明文;生产请用 BCrypt) */
    private String userPswd;
}
