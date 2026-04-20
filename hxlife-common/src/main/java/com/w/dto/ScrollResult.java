package com.w.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "滚动结果")
@Data
public class ScrollResult {
    @Schema(description = "列表")
    private List<?> list;
    @Schema(description = "最小时间")
    private Long minTime;
    @Schema(description = "偏移量")
    private Integer offset;
}
