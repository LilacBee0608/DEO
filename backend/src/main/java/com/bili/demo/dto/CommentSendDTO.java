package com.bili.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 发送评论参数 DTO
 */
@Data
public class CommentSendDTO {

    /**
     * 视频id
     * 字段名 vId 会导致 Lombok 生成的 getVId() 被 Jackson 误推断为 vid
     * 需要同时在字段、getter、setter 上加 @JsonProperty("vId") 让 Jackson 合并为同一个属性
     */
    @NotBlank(message = "视频id不能为空")
    @Getter(onMethod_ = @JsonProperty("vId"))
    @Setter(onMethod_ = @JsonProperty("vId"))
    @JsonProperty("vId")
    private String vId;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 100, message = "评论最长100字符")
    private String commentContent;
}
