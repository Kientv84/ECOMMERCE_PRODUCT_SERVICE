package com.ecommerce.kientv84.controller;

import com.ecommerce.kientv84.commons.upload.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("v1/api")
@RequiredArgsConstructor
public class FileController {
    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<String> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "others") String folder
    ) {
        String url = fileStorageService.uploadImage(file, folder);
        return ResponseEntity.ok(url);
    }

    // ---- Upload manny file ----
    @PostMapping("/upload/many")
    public ResponseEntity<List<String>> uploadMany(
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam(value = "folder", defaultValue = "others") String folder
    ) {
        List<String> urls = fileStorageService.uploadManyImage(files, folder);
        return ResponseEntity.ok(urls);
    }
}
