package com.whx.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Component
public class ScrollResult {
    private List<Blog> data;
    private Long minTime;
    private Integer offset;
}
