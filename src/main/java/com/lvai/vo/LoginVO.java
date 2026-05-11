package com.lvai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录响应")
public class LoginVO {

    @Schema(description = "Token")
    private String token;

    @Schema(description = "Token过期时间(秒)")
    private Long tokenTimeout;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "是否新用户")
    private Boolean isNew;
}
