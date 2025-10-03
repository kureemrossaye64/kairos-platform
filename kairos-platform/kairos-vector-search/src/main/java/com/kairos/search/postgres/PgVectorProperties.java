package com.kairos.search.postgres;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PgVectorProperties {
	
	private String host;
	
	private Integer port;
	
	private String username;
	
	private String password;
	
	@Builder.Default
	private String tableName = "embeddings";

	@Builder.Default
	private Integer dimension = 384;

	private String database;
}