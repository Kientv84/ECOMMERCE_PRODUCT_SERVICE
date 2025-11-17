package com.ecommerce.kientv84.commons.upload;

import com.ecommerce.kientv84.commons.EnumError;
import com.ecommerce.kientv84.exceptions.ServiceException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class ImageValidator {

    public void validateImage(MultipartFile file) {

        if (file.isEmpty()) {
            throw new ServiceException(EnumError.FILE_EMPTY, "file.empty");
        }

        String ext = file.getOriginalFilename()
                .substring(file.getOriginalFilename().lastIndexOf('.') + 1)
                .toLowerCase();

        List<String> allowed = List.of("jpg", "jpeg", "png", "webp");

        if (!allowed.contains(ext)) {
            throw new ServiceException(EnumError.FILE_INVALID, "file.invalid");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new ServiceException(EnumError.FILE_TOO_LARGE, "file.too.large");
        }
    }

    public void validateMany(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new ServiceException(EnumError.FILE_EMPTY, "file.empty");
        }

        for (MultipartFile file : files) {
            validateImage(file);
        }
    }

}

