package com.kairos.ingestion.source;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import org.apache.tika.Tika;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;

import com.kairos.core.ingestion.IIngestionRouter;
import com.kairos.core.ingestion.IngestionRequest;
import com.kairos.ingestion.configs.FileIngestionProperties;
import com.kairos.ingestion.utils.LocalFileMultipartFile;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kairos.ingestion.watcher.enabled", havingValue = "true")
public class FolderWatcherService {

    private final FileIngestionProperties properties;
    private final IIngestionRouter ingestionRouter;
    private final Tika tika = new Tika();

    @PostConstruct
    public void init() {
        createDirectoryIfMissing(properties.getInputDir());
        createDirectoryIfMissing(properties.getProcessedDir());
        log.info("Folder Watcher started. Monitoring: {}", properties.getInputDir());
    }

    @Scheduled(fixedDelayString = "${kairos.ingestion.watcher.poll-interval-ms:5000}")
    public void scanFolder() {
        File inputFolder = new File(properties.getInputDir());
        File[] files = inputFolder.listFiles(File::isFile);

        if (files == null || files.length == 0) {
            return;
        }

        for (File file : files) {
            // Ignore hidden files (like .DS_Store or tmp files)
            if (file.isHidden() || file.getName().startsWith(".")) {
                continue;
            }

            // Attempt to ensure file is fully written (simple check)
            if (!isFileReady(file)) {
                log.debug("File {} is not ready yet (locked or writing). Skipping...", file.getName());
                continue;
            }

            processFile(file);
        }
    }

    private void processFile(File file) {
        log.info("Detected new file: {}", file.getName());
        try {
            // 1. Detect Content Type
            String contentType = tika.detect(file);
            
            // 2. Prepare Metadata Manifest
            Map<String, Object> manifest = new HashMap<>();
            manifest.put("source_origin", "folder_watcher");
            manifest.put("original_path", file.getAbsolutePath());
            manifest.put("ingestion_method", "automatic_drop");

            // 3. Create Ingestion Request
            LocalFileMultipartFile multipartFile = new LocalFileMultipartFile(file, contentType);
            IngestionRequest request = IngestionRequest.from(multipartFile, manifest);

            // 4. Trigger Ingestion Pipeline
            // This will upload to GCS, create DB record, and trigger Async events
            ingestionRouter.addRequest(request);
            log.info("Ingestion triggered for: {}", file.getName());

            // 5. Move to Processed Folder
            moveFileToProcessed(file);

        } catch (Exception e) {
            log.error("Failed to ingest file: {}", file.getName(), e);
            moveFileToError(file);
        }
    }

    private void moveFileToProcessed(File file) {
        try {
            Path targetDir = Paths.get(properties.getProcessedDir());
            Path targetPath = targetDir.resolve(file.getName());

            // Handle duplicate names by appending timestamp if exists
            if (Files.exists(targetPath)) {
                String name = file.getName();
                String baseName = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name;
                String extension = name.contains(".") ? name.substring(name.lastIndexOf('.')) : "";
                String newName = baseName + "_" + System.currentTimeMillis() + extension;
                targetPath = targetDir.resolve(newName);
            }

            Files.move(file.toPath(), targetPath, StandardCopyOption.ATOMIC_MOVE);
            log.info("Moved file to: {}", targetPath);
        } catch (IOException e) {
            log.error("Failed to move file {} to processed folder", file.getName(), e);
        }
    }
    
    private void moveFileToError(File file) {
        // Simple error handling: rename to .error or move to an error folder
        // For now, let's create an 'error' subdir in processed
        try {
            Path errorDir = Paths.get(properties.getProcessedDir(), "errors");
            if (!Files.exists(errorDir)) Files.createDirectories(errorDir);
            
            Path targetPath = errorDir.resolve(file.getName() + ".failed");
            Files.move(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.warn("Moved failed file to: {}", targetPath);
        } catch (IOException e) {
            log.error("Could not move failed file", e);
        }
    }

    private boolean isFileReady(File file) {
        // Basic check: can we read/write and is size stable?
        // In a real high-volume production, we might verify checksums or use FileLock
        return file.exists() && file.canRead() && file.canWrite();
    }

    private void createDirectoryIfMissing(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                log.info("Created directory: {}", path);
            }
        }
    }
}