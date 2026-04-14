package com.kairos.autoconfigure;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.kairos.autoconfigure.properties.KairosProperties;
import com.kairos.core.storage.StorageService;
import com.kairos.storage.gcs.GcsStorageService;
import com.kairos.storage.local.LocalStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@Slf4j
@AutoConfiguration
public class KairosStorageAutoConfiguration {

    // =================================================================================
    // 1. LOCAL FILESYSTEM (DEFAULT)
    // =================================================================================
    @Bean
    @ConditionalOnMissingBean(StorageService.class)
    @ConditionalOnProperty(prefix = "kairos.storage", name = "provider", havingValue = "LOCAL", matchIfMissing = true)
    public StorageService localStorageService(KairosProperties props) {
        String rootDir = props.getStorage().getLocal().getRootDirectory();
        log.info("KAIROS: Using Local Filesystem Storage at '{}'", rootDir);
        return new LocalStorageService(rootDir);
    }

    // =================================================================================
    // 2. GOOGLE CLOUD STORAGE
    // =================================================================================
    @Bean
    @ConditionalOnProperty(prefix = "kairos.storage", name = "provider", havingValue = "GCS")
    @ConditionalOnClass(Storage.class) // Only if google-cloud-storage is in classpath
    public StorageService gcsStorageService(KairosProperties props) {
        KairosProperties.GcsProperties gcsProps = props.getStorage().getGcs();
        String projectId = gcsProps.getProjectId();
        String bucket = gcsProps.getBucketName();

        if (bucket == null) {
            throw new IllegalStateException("kairos.storage.gcs.bucket-name is required when GCS provider is enabled.");
        }

        log.info("KAIROS: Using Google Cloud Storage (Bucket: {})", bucket);

        // Auto-detect credentials via Google Cloud default chain (Env vars, CLI, etc.)
        StorageOptions.Builder builder = StorageOptions.newBuilder();
        if (projectId != null && !projectId.isBlank()) {
            builder.setProjectId(projectId);
        }
        
        return new GcsStorageService(builder.build().getService(), bucket);
    }
}