package com.kairos.vector_search.config;

import static dev.langchain4j.internal.Utils.isNullOrBlank;
import static java.util.Collections.singletonList;

import java.net.URISyntaxException;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.message.BasicHeader;
import org.n52.jackson.datatype.jts.JtsModule;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.opensearch.OpenSearchEmbeddingStore;
import dev.langchain4j.store.embedding.opensearch.OpenSearchRequestFailedException;
import lombok.extern.slf4j.Slf4j;

/**
 * Configures the connection to the ChromaDB vector store. This configuration is
 * activated only when "kairos.vector-store.chroma.base-url" is set.
 */
@Configuration
@ConditionalOnProperty(name = "kairos.vector-store.opensearch.base-url")
@EnableConfigurationProperties(OpenSearchProperties.class)
@Slf4j
public class VectorStoreConfig {
	
	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper m = new ObjectMapper();
		m.registerModule(new Jdk8Module());
		m.registerModule(new JavaTimeModule());
		m.registerModule(new JtsModule());
		return m;
	}

	@Bean
	public OpenSearchClient openSearchClient(OpenSearchProperties props) {
		String serverUrl = props.getBaseUrl();
		String apiKey = props.getApiKey();
		String userName = props.getUsername();
		String password = props.getPassword();
		HttpHost openSearchHost;
		try {
			openSearchHost = HttpHost.create(serverUrl);
		} catch (URISyntaxException se) {
			log.error("[I/O OpenSearch Exception]", se);
			throw new OpenSearchRequestFailedException(se.getMessage());
		}
		
		JacksonJsonpMapper mapper = new JacksonJsonpMapper(objectMapper());

		OpenSearchTransport transport = ApacheHttpClient5TransportBuilder.builder(openSearchHost)
				.setMapper(mapper).setHttpClientConfigCallback(httpClientBuilder -> {

					if (!isNullOrBlank(apiKey)) {
						httpClientBuilder
								.setDefaultHeaders(singletonList(new BasicHeader("Authorization", "ApiKey " + apiKey)));
					}

					if (!isNullOrBlank(userName) && !isNullOrBlank(password)) {
						BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
						credentialsProvider.setCredentials(new AuthScope(openSearchHost),
								new UsernamePasswordCredentials(userName, password.toCharArray()));
						httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
					}

					httpClientBuilder.setConnectionManager(PoolingAsyncClientConnectionManagerBuilder.create().build());

					return httpClientBuilder;
				}).build();

		OpenSearchClient client = new OpenSearchClient(transport);
		
		return client;
	}

	@Bean
	public EmbeddingStore<TextSegment> opensearchEmbeddingStore(OpenSearchProperties props) {
		EmbeddingStore<TextSegment> embeddingStore = OpenSearchEmbeddingStore.builder().serverUrl(props.getBaseUrl()) 
				.indexName(props.getIndexName())
				.apiKey(props.getApiKey()) // Optional: for secured OpenSearch instances
				.userName(props.getUsername()) // Optional: for secured OpenSearch instances
				.password(props.getPassword()) // Optional: for secured OpenSearch instances
				
				.build();

		return embeddingStore;
	}
}