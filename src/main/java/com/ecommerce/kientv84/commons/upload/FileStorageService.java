package com.ecommerce.kientv84.commons.upload;

import com.ecommerce.kientv84.services.UploadFileProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final ImageValidator validator;
    private final UploadFileProvider storageProvider;

    public String uploadImage(MultipartFile file, String folder) {
        validator.validateImage(file);
        return storageProvider.upload(file, folder);
    }

    public List<String> uploadManyImage(List<MultipartFile> files, String folder) {
        validator.validateMany(files);
        return storageProvider.uploadMany(files, folder);
    }
}
