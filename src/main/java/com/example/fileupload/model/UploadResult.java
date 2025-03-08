package com.example.fileupload.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UploadResult {
	private String filename;
	private long fileSize;
	private String path;
	private String method;
	private long uploadTimeMs;
	private long memoryUsedBytes;
}
