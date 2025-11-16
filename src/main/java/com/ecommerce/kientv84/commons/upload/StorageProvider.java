package com.ecommerce.kientv84.commons.upload;

import org.springframework.web.multipart.MultipartFile;

public interface StorageProvider { //provider lưu trữ file nhận vào các file và trả ra string
    String upload(MultipartFile file);
}
