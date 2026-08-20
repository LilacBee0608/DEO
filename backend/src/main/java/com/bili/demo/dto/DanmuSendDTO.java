package com.bili.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 发送弹幕参数 DTO
 * vId:         视频id
 * danmuContent: 弹幕文本
 * danmuFrame:  弹幕出现时间(秒)
 * color:       弹幕颜色(可空,默认白色)
 */
@Data
public class DanmuSendDTO {

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

    @NotBlank(message = "弹幕内容不能为空")
    @Size(max = 100, message = "弹幕最长100字符")
    private String danmuContent;

    @NotNull(message = "弹幕时间不能为空")
    private Integer danmuFrame;

    /** 弹幕颜色(可空,后端默认白色) */
    private String color;
}
