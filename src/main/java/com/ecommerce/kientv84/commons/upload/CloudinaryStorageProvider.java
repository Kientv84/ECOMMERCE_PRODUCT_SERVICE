package com.ecommerce.kientv84.commons.upload;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CloudinaryStorageProvider implements StorageProvider { //upload file lên Cloudinary.

    private final Cloudinary cloudinary;  // Inject Cloudinary bean

    @Override
    public String upload(MultipartFile file) {

        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", "ecommerce") // optional folder
            );

            return uploadResult.get("secure_url").toString();

        } catch (Exception e) {
            throw new RuntimeException("Upload failed: " + e.getMessage());
        }
    }
}

//Nhận MultipartFile từ client.
//Lấy bytes từ file (file.getBytes()).
//Gọi API Cloudinary (cloudinary.uploader().upload) với tùy chọn folder "ecommerce".
//Lấy URL bảo mật secure_url từ kết quả trả về.
//Trả URL về service.