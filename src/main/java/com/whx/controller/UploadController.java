package com.whx.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.whx.dto.Result;
import com.whx.service.IUploadService;
import com.whx.utils.SystemConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Tag(name = "图片加载接口")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("upload")
public class UploadController {

    private final IUploadService uploadService;
    @Operation(summary = "图片上传")
    @PostMapping("/image")
    public Result uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        if(file.isEmpty()){
            return Result.fail("图片不能为空");
        }
        String url=uploadService.uploadImage(file);
        if(url==null){
            return Result.fail("图片上传失败");
        }
        return Result.ok(url);
    }


}
