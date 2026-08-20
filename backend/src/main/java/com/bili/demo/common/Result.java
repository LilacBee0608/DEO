package com.bili.demo.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结果封装类
 * 前端根据 code 字段判断请求是否成功:
 *   - 200 表示成功
 *   - 其他码表示失败,通过 message 字段返回错误信息
 *
 * @param <T> data 字段的数据类型
 */
@Data
public class Result<T> implements Serializable {

    private Integer code;
    private String message;
    private T data;

    private Result() {}

    /** 成功(无数据) */
    public static <T> Result<T> success() {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage("success");
        return r;
    }

    /** 成功(带数据) */
    public static <T> Result<T> success(T data) {
        Result<T> r = success();
        r.setData(data);
        return r;
    }

    /** 成功(自定义消息+数据) */
    public static <T> Result<T> success(String message, T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage(message);
        r.setData(data);
        return r;
    }

    /** 失败(自定义错误码+消息) */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    /** 失败(默认500) */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
}
