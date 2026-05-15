package com.lvai.service;

import org.springframework.web.multipart.MultipartFile;

public interface IFileService {
    String uploadFile(MultipartFile file);
    void deleteFile(String fileUrl);
}
