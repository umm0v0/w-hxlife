package com.whx.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户DTO")
@Data
public class UserDTO {
    @Schema(description = "用户id")
    private Long id;
    @Schema(description = "昵称")
    private String nickName;
    @Schema(description = "图标")
    private String icon;
}
