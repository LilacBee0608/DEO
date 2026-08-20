package com.bili.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发送评论参数 DTO
 */
@Data
public class CommentSendDTO {

    @NotBlank(message = "视频id不能为空")
    @JsonProperty("vId")
    private String vId;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 100, message = "评论最长100字符")
    private String commentContent;
}
