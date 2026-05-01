package com.company.product.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class StorageService {
    private final Path rootPath;

    public StorageService(@Value("${app.storage.root-dir}") String rootDir) throws IOException {
        this.rootPath = Path.of(rootDir).toAbsolutePath();
        Files.createDirectories(rootPath);
        Files.createDirectories(rootPath.resolve("avatars"));
        Files.createDirectories(rootPath.resolve("dishes"));
    }

    public String store(MultipartFile file, String folder) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Файл пустой");
        }
        String extension = "";
        String originalName = file.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        String filename = UUID.randomUUID() + extension;
        Path target = rootPath.resolve(folder).resolve(filename);
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось сохранить файл", e);
        }
        return "/uploads/" + folder + "/" + filename;
    }
}
