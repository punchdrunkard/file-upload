package com.example.fileupload.controller;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fileupload.model.UploadResult;
import com.example.fileupload.service.FileStorageService;
import com.example.fileupload.util.PerformanceMonitor;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/stream")
@RequiredArgsConstructor
@Slf4j
public class StreamUploadController {
	private final FileStorageService storageService;
	private final PerformanceMonitor performanceMonitor;

	@PostMapping("/upload")
	public UploadResult uploadFile(
		@RequestHeader("X-File-Name") String encodedFilename,
		@RequestHeader("X-File-Size") long fileSize,
		HttpServletRequest request) {

		// 요청 시작 시간 가져오기
		Long requestStartTime = (Long) request.getAttribute("requestStartTime");
		if (requestStartTime == null) {
			requestStartTime = System.currentTimeMillis(); // 필터가 없는 경우 대비
		}

		// 파일명 디코딩 (안전하게 처리)
		String filename;
		try {
			filename = URLDecoder.decode(encodedFilename, StandardCharsets.UTF_8.name());
		} catch (Exception e) {
			log.warn("파일명 디코딩 실패, 원본 사용: {}", encodedFilename, e);
			filename = encodedFilename;
		}

		final String finalFilename = filename;
		final long finalRequestStartTime = requestStartTime;

		return performanceMonitor.monitorUpload("stream", fileSize, () -> {
			try (ServletInputStream inputStream = request.getInputStream()) {
				// 서비스 메서드 시작 시간
				long serviceStartTime = System.currentTimeMillis();

				// 파일 저장 처리
				String savedPath = storageService.saveInputStream(inputStream, finalFilename, "stream");

				// 서비스 메서드 완료 시간
				long serviceEndTime = System.currentTimeMillis();

				// 서비스 메서드 실행 시간
				long serviceMethodTime = serviceEndTime - serviceStartTime;

				// 전체 업로드 시간 (요청 시작부터 완료까지)
				long totalUploadTime = serviceEndTime - finalRequestStartTime;

				log.info("Service method time: {}ms", serviceMethodTime);
				log.info("Total upload time: {}ms", totalUploadTime);

				return UploadResult.builder()
					.filename(finalFilename)
					.fileSize(fileSize)
					.path(savedPath)
					.method("stream")
					.uploadTimeMs(totalUploadTime) // 전체 시간 반영
					.build();
			} catch (IOException e) {
				log.error("Failed to upload stream file", e);
				throw new RuntimeException("Failed to upload file", e);
			}
		});
	}
}
