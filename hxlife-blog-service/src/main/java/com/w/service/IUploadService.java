package com.w.service;

import org.springframework.web.multipart.MultipartFile;

public interface IUploadService {
    String uploadImage(MultipartFile file);
}
