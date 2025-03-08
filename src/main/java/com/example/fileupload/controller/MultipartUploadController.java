package com.example.fileupload.controller;

import java.io.IOException;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.fileupload.model.UploadResult;
import com.example.fileupload.service.FileStorageService;
import com.example.fileupload.util.PerformanceMonitor;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/multipart")
@RequiredArgsConstructor
@Slf4j
public class MultipartUploadController {
	private final FileStorageService storageService;
	private final PerformanceMonitor performanceMonitor;

	@PostMapping("/upload")
	public UploadResult uploadFile(@RequestParam("file") MultipartFile file,  HttpServletRequest request) throws IOException {
		return performanceMonitor.monitorUpload("multipart", file.getSize(), () -> {
			try {
				String filename = StringUtils.cleanPath(file.getOriginalFilename());

				long beforeProcess = System.currentTimeMillis();
				String savedPath = storageService.saveMultipartFile(file, "multipart");
				long afterProcess = System.currentTimeMillis();

				log.info("Service method time: {}ms", afterProcess - beforeProcess);

				long totalUploadTime = afterProcess - beforeProcess;

				return UploadResult.builder()
					.filename(filename)
					.fileSize(file.getSize())
					.path(savedPath)
					.method("multipart")
					.uploadTimeMs(totalUploadTime)
					.build();
			} catch (IOException e) {
				log.error("Failed to upload multipart file", e);
				throw new RuntimeException("Failed to upload file", e);
			}
		});
	}
}
