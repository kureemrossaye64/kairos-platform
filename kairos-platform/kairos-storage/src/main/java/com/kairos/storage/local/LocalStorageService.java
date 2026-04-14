package com.kairos.storage.local;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.kairos.core.storage.StorageService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalStorageService implements StorageService {

    private final Path rootDirectory;

    public LocalStorageService(String rootPath) {
        this.rootDirectory = Paths.get(rootPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootDirectory);
            log.info("Local Storage initialized at: {}", this.rootDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    @Override
    public String upload(InputStream inputStream, String fileName, String contentType) {
        try {
            // sanitize filename to prevent directory traversal
            String safeFileName = Paths.get(fileName).normalize().toString();
            Path destinationFile = this.rootDirectory.resolve(safeFileName).normalize();

            // Security check
            if (!destinationFile.startsWith(this.rootDirectory)) {
                throw new SecurityException("Cannot store file outside current directory.");
            }

            // Ensure parent directories exist
            Files.createDirectories(destinationFile.getParent());

            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            
            // Return a URI that represents this file
            return destinationFile.toUri().toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + fileName, e);
        }
    }

    @Override
    public InputStream download(String fileUri) {
        try {
            Path path = parseUri(fileUri);
            return new FileInputStream(path.toFile());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + fileUri, e);
        }
    }

    @Override
    public ReadableByteChannel downloadChannel(String fileUri) {
        try {
            Path path = parseUri(fileUri);
            return Files.newByteChannel(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to open channel for: " + fileUri, e);
        }
    }
    
    // Helper to resolve URI back to Path
    private Path parseUri(String fileUri) {
        try {
            URI uri = URI.create(fileUri);
            // Handle file:/// vs just /paths
            if ("file".equals(uri.getScheme())) {
                return Paths.get(uri);
            }
            return Paths.get(fileUri);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid URI format: " + fileUri, e);
        }
    }
}