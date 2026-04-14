package com.kairos.ingestion.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import com.kairos.core.ingestion.MultipartFile;

import lombok.RequiredArgsConstructor;

/**
 * Adapts a local java.io.File to the internal MultipartFile interface
 * required by the IngestionRequest.
 */
@RequiredArgsConstructor
public class LocalFileMultipartFile implements MultipartFile {

    private final File file;
    private final String contentType;

    @Override
    public String getName() {
        return "file";
    }

    @Override
    public String getOriginalFilename() {
        return file.getName();
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return file.length() == 0;
    }

    @Override
    public long getSize() {
        return file.length();
    }

    @Override
    public byte[] getBytes() {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read bytes from local file", e);
        }
    }

    @Override
    public InputStream getInputStream() {
        try {
            return new FileInputStream(file);
        } catch (IOException e) {
            // Fallback to byte array if stream fails, though unusual for local file
            return new ByteArrayInputStream(new byte[0]); 
        }
    }
}