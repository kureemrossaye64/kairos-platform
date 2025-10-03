package com.kairos.sports_atlas.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.kairos.core.storage.StorageService;
import com.kairos.storage.gcs.GcsProperties;
import com.kairos.storage.gcs.GcsStorageService;

@Configuration
public class StorageConfiguration {
	
	@Bean
	public GcsProperties gcsProperties(@Value("${kairos.storage.gcs.bucket-name}") String bucketName, @Value("${kairos.storage.gcs.project-id}")String projectId) {
		return GcsProperties.builder().bucketName(bucketName).projectId(projectId).build();
	}
	
	public GoogleCredentials getGoogleCredential() throws IOException {
		GoogleCredentials credentials = GoogleCredentials.fromStream(Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("foodguard-453108-5d360d30dd86.json"));
		return credentials;
	}
	
	@Bean
	public Storage getStorage(GcsProperties prop) throws IOException {
		
			return StorageOptions.newBuilder().setCredentials(getGoogleCredential()).setProjectId(prop.getProjectId())
					.build().getService();
	}
	
	@Bean
	public StorageService storageService(Storage client, GcsProperties props) {
		return new GcsStorageService(client,props);
	}
}