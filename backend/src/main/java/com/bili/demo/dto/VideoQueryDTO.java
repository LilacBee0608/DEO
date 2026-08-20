package com.bili.demo.dto;

import lombok.Data;

/**
 * 视频分页查询参数 DTO
 * 默认第1页,每页12条(仿B站首页布局)
 */
@Data
public class VideoQueryDTO {
    /** 当前页码(默认1) */
    private Integer page = 1;
    /** 每页条数(默认12) */
    private Integer size = 12;
    /** 标题模糊关键词 */
    private String title;
    /** 标签模糊关键词 */
    private String tags;
}
