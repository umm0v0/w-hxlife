package com.whx.controller;


import com.whx.dto.Result;
import com.whx.service.IFollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author wumm
 * @since 2026-04-10
 */

@Tag(name = "关注接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/follow")
public class FollowController {

    private final IFollowService followService;

    @Operation(summary = "关注/取关")
    @PutMapping("/{id}/{isFollow}")
    public Result follow(@PathVariable(name = "id")Long id,@PathVariable(name = "isFollow")Boolean isFollow){
        return followService.follow(id,isFollow);
    }

    @Operation(summary = "共同关注")
    @GetMapping("/common/{id}")
    public Result commonFollows(@PathVariable(name = "id")Long id){
        return followService.commonFollows(id);
    }

}
