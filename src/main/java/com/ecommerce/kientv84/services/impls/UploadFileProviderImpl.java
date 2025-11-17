package com.ecommerce.kientv84.services.impls;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ecommerce.kientv84.services.UploadFileProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UploadFileProviderImpl implements UploadFileProvider {

    private final Cloudinary cloudinary;

    @Override
    public String upload(MultipartFile file, String folder) {
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", "ecommerce/" + folder)
            );

            return uploadResult.get("secure_url").toString();

        } catch (Exception e) {
            throw new RuntimeException("Upload failed: " + e.getMessage());
        }
    }

    @Override
    public List<String> uploadMany(List<MultipartFile> files, String folder) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(upload(file, folder));
        }
        return urls;
    }

    @Override
    public void deleteFileFromCloudinary(String url) {
        try {
            String publicId = extractPublicId(url);
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("invalidate", true));
        } catch (Exception e) {
            System.err.println("Failed to delete Cloudinary file: " + url);
        }
    }


    /**
     * Extract Cloudinary public_id from full URL.
     *
     * Ví dụ URL:
     * https://res.cloudinary.com/root/image/upload/v1731827360/ecommerce/brands/123/abc999.jpg
     *
     * Kết quả trả về:
     * ecommerce/brands/123/abc999
     */
    private String extractPublicId(String url) {
        try {
            String[] parts = url.split("/");

            // Lấy tên file: abc999.jpg
            String fileName = parts[parts.length - 1];

            // Bỏ phần .jpg / .png / .webp
            String fileNameNoExt = fileName.substring(0, fileName.lastIndexOf("."));

            // Tìm vị trí bắt đầu của folder "ecommerce"
            int idx = -1;
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals("ecommerce")) {
                    idx = i;
                    break;
                }
            }

            if (idx == -1) return null;

            // Ghép lại từ "ecommerce/.../fileName"
            StringBuilder publicId = new StringBuilder("ecommerce");
            for (int i = idx + 1; i < parts.length - 1; i++) {
                publicId.append("/").append(parts[i]);
            }

            publicId.append("/").append(fileNameNoExt);

            return publicId.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
