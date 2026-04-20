package com.w.controller;

import com.w.dto.Result;
import com.w.service.IUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
