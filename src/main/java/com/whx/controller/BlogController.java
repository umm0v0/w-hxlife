package com.whx.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.whx.dto.Result;
import com.whx.dto.UserDTO;
import com.whx.entity.Blog;
import com.whx.entity.User;
import com.whx.service.IBlogService;
import com.whx.service.IUserService;
import com.whx.utils.SystemConstants;
import com.whx.utils.UserHolder;
import javax.annotation.Resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author wumm
 * @since 2026-04-10
 */
@Tag(name = "博客接口")
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Resource
    private IBlogService blogService;
    @Resource
    private IUserService userService;

    @Operation(summary = "保存博客")
    @PostMapping
    public Result saveBlog(@RequestBody Blog blog) {
        return blogService.saveBlog(blog);
    }

    @Operation(summary = "点赞博客")
    @PutMapping("/like/{id}")
    public Result likeBlog(@PathVariable("id") Long id) {
        return blogService.likeBlog(id);
    }




    @GetMapping("/of/me")
    public Result queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        // 根据用户查询
        Page<Blog> page = blogService.query()
                .eq("user_id", user.getId()).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        return Result.ok(records);
    }

    @GetMapping("/hot")
    public Result queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 根据用户查询
        Page<Blog> page = blogService.query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        // 查询用户
        records.forEach(blog ->{
            Long userId = blog.getUserId();
            User user = userService.getById(userId);
            blog.setName(user.getNickName());
            blog.setIcon(user.getIcon());
        });
        return Result.ok(records);
    }

    @Operation(summary = "查动态")
    @GetMapping("/of/follow")
    public Result queryFollowBlog(@RequestParam(value = "lastTime")Long lastTime,
                                  @RequestParam(value = "offset",defaultValue = "0")Long offset
                                  ) {
        if(lastTime==null){
            lastTime= System.currentTimeMillis();
        }
        return blogService.queryFollowBlog(lastTime,offset);
    }


}
