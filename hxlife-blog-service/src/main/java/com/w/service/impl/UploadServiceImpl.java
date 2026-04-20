package com.w.service.impl;

import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import com.w.config.OssProperties;
import com.w.service.IUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UploadServiceImpl implements IUploadService {

    private final OssProperties ossProperties;

    private static final List<String>IMAGE_EXTENSIONS=List.of(".jpg",".jpeg",".png",".gif",".webp");

    @Override
    public String uploadImage(MultipartFile file) {
        //创建OSSclient
        OSSClient ossClient= (OSSClient) new OSSClientBuilder().build(ossProperties.getEndpoint(),ossProperties.getAccessKeyId(),ossProperties.getAccessKeySecret());
        try{
            //还要注意从前端可能传过来
            //获得原始文件名，防止覆盖
            String originalFilename=file.getOriginalFilename();
            //一次校验
            if(StrUtil.isBlank(originalFilename)){
                throw new IllegalArgumentException("文件名不能为空");
            }
            //二次校验
            String extension=originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if(StrUtil.isBlank(extension)||!IMAGE_EXTENSIONS.contains(extension)){
                throw new IllegalArgumentException("文件扩展名错误");
            }
            //三次校验
            String contentType=file.getContentType();
            if(StrUtil.isBlank(contentType)||!contentType.startsWith("image/")){
                throw new IllegalArgumentException("文件类型错误");
            }

            String fileName= UUID.randomUUID().toString()+extension;

            //按照日期分类
            String datePath= LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fullPath=datePath+"/"+fileName;

            //执行上传
            ossClient.putObject(ossProperties.getBucketName(),fullPath,file.getInputStream());
            return "https://"+ossProperties.getBucketName()+"."+ossProperties.getEndpoint()+"/"+fullPath;


        }catch (IOException e){
            throw new RuntimeException(e);

        } finally{
            if(ossClient!=null) {
                ossClient.shutdown();
            }
        }


    }
}
