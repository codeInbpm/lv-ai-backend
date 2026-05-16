package com.lvai.service;

import org.springframework.web.multipart.MultipartFile;

public interface IFileService {
    String uploadFile(MultipartFile file);
    String uploadFile(MultipartFile file, String folder);
    void deleteFile(String fileUrl);
}
