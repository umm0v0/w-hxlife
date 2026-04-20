package com.w.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.w.client.UserClient;
import com.w.dto.Result;
import com.w.dto.UserDTO;
import com.w.entity.Blog;
import com.w.entity.User;
import com.w.service.IBlogService;
import com.w.utils.SystemConstants;
import com.w.utils.UserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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
    private UserClient userService;

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
