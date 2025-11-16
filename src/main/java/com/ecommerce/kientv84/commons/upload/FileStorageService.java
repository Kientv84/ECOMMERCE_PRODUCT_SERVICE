package com.ecommerce.kientv84.commons.upload;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final ImageValidator validator;
    private final StorageProvider storageProvider; // Inject provider (Local, S3, Cloudinary...)

    public String uploadImage(MultipartFile file) {
        validator.validateImage(file);
        return storageProvider.upload(file);
    }
}

