package com.ecommerce.kientv84.services;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UploadFileProvider { //provider lưu trữ file nhận vào các file và trả ra string
    String upload(MultipartFile file, String folder);

    List<String> uploadMany(List<MultipartFile> files, String folder);

    void deleteFileFromCloudinary(String url);
}
