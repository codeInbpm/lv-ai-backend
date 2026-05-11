package com.lvai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "微信登录请求")
public class WxLoginDTO {

    @NotBlank(message = "code不能为空")
    @Schema(description = "微信临时登录凭证code")
    private String code;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "性别: 0未知 1男 2女")
    private Integer gender;
}
