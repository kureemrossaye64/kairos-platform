package com.kairos.core.ingestion;

import java.io.InputStream;

public interface MultipartFile {

	public String getName();

	public String getOriginalFilename();

	public String getContentType();

	public boolean isEmpty();

	public long getSize();

	public byte[] getBytes();

	public InputStream getInputStream();

}
