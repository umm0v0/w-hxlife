package com.whx.controller;


import com.whx.dto.LoginFormDTO;
import com.whx.dto.Result;
import com.whx.entity.UserInfo;
import com.whx.service.IUserInfoService;
import com.whx.service.IUserService;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


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





}
