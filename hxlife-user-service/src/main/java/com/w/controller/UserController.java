package com.w.controller;


import cn.hutool.core.util.StrUtil;
import com.w.dto.LoginFormDTO;
import com.w.dto.Result;
import com.w.entity.User;
import com.w.service.IUserService;
import com.w.utils.UserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;


@Tag(name = "用户登录接口")
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private IUserService userService;

    @Operation(summary = "发送验证码")
    @PostMapping("/code")
    public Result sendCode(@RequestParam("phone") String phone) {
        return userService.sendCode(phone);
    }

    @Operation(summary ="登录")
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginFormDTO) {
        return userService.login(loginFormDTO);
    }

    @Operation(summary ="根据id查询用户")
    @GetMapping("/{id}")
    public Result queryUserById(@PathVariable(name = "id")Long id){
        return Result.ok(userService.getById(id));
    }

    @Operation(summary ="用户签到接口")
    @PostMapping("/sign")
    public Result sign(){
        return userService.sign();
    }

    @Operation(summary ="用户签到统计")
    @GetMapping("/sign/count")
    public Result signCount(){
        return userService.signCount();
    }

    @GetMapping("/{userId}")
    public User getById(@PathVariable("userId") Long userId){
        return userService.getById(userId);
    }

    @GetMapping("/list")
    public List<User> list(@RequestParam("ids") Collection<Long> ids){
        String idsStr= StrUtil.join(",",ids);
        return  userService.lambdaQuery().in(User::getId,ids).last("order by field(id,"+idsStr+")").list();
    }




}
