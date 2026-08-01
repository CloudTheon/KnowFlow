package com.cloudtheon.knowflowcore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 提交反馈请求
 */
@Data
@Schema(description = "提交反馈请求")
public class FeedbackRequest {

    @Schema(description = "反馈类型：bug=问题反馈 / suggestion=功能建议 / other=其他", example = "suggestion")
    @NotBlank(message = "反馈类型不能为空")
    @Pattern(regexp = "bug|suggestion|other", message = "反馈类型不合法")
    private String type;

    @Schema(description = "反馈内容", example = "希望支持更多文档格式")
    @NotBlank(message = "反馈内容不能为空")
    @Size(max = 2000, message = "反馈内容过长（最多 2000 字）")
    private String content;

    @Schema(description = "联系方式（可选）", example = "zhangsan@example.com")
    @Size(max = 100, message = "联系方式过长")
    private String contact;
}
