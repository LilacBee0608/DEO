package com.bili.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 视频上传/修改参数 DTO
 * 不含 v_id(创建时由后端生成)、play_num 等统计字段(后端默认0)
 */
@Data
public class VideoUploadDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最长100字符")
    private String title;

    @Size(max = 10, message = "标签最长10字符")
    private String tags;

    @Size(max = 1000, message = "简介过长")
    private String description;

    /** 封面URL(可空,前端上传后返回URL) */
    private String coverUrl;

    /** 视频URL(可空,前端上传后返回URL) */
    private String videoUrl;
}
