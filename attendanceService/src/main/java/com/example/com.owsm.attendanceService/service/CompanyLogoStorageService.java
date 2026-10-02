package com.example.attendanceService.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CompanyLogoStorageService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private final Path uploadDirectory;

    public CompanyLogoStorageService(@Value("${app.file.upload-dir:uploads}") String uploadDirectory) {
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize()
            .resolve("company-logos");
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A logo image is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Logo image must be 5 MB or smaller");
        }

        String contentType = file.getContentType();
        String extension;
        if ("image/png".equalsIgnoreCase(contentType)) {
            extension = ".png";
        } else if ("image/jpeg".equalsIgnoreCase(contentType)) {
            extension = ".jpg";
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Logo must be a PNG or JPEG image");
        }

        try {
            byte[] contents = file.getBytes();
            var image = ImageIO.read(new java.io.ByteArrayInputStream(contents));
            if (image == null || image.getWidth() > 4096 || image.getHeight() > 4096) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Logo image is invalid or too large");
            }
            String filename = UUID.randomUUID().toString().toLowerCase(Locale.ROOT) + extension;
            Files.createDirectories(uploadDirectory);
            Files.write(uploadDirectory.resolve(filename), contents);
            return filename;
        } catch (IOException exception) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Could not store department logo",
                exception
            );
        }
    }

    public Path resolve(String filename) {
        if (filename == null || !filename.matches("(?i)[0-9a-f-]{36}\\.(png|jpg)")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Logo not found");
        }
        Path file = uploadDirectory.resolve(filename).normalize();
        if (!file.startsWith(uploadDirectory) || !Files.isRegularFile(file)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Logo not found");
        }
        return file;
    }
}
