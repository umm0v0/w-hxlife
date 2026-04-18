package com.whx.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(name = "登录表单")
@Data
public class LoginFormDTO {
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "验证码")
    private String code;
    @Schema(description = "密码")
    private String password;
}
