package com.example.fileupload.util;

import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.example.fileupload.model.UploadResult;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class PerformanceMonitor {
	private final MeterRegistry meterRegistry;

	public <T> T monitorUpload(String method, long fileSize, Supplier<T> uploadFunction) {
		Timer timer = Timer.builder("file.upload.time")
			.tag("method", method)
			.tag("size", formatFileSize(fileSize))
			.register(meterRegistry);

		long startMemory = getUsedMemory();
		Timer.Sample sample = Timer.start(meterRegistry);

		T result = uploadFunction.get();

		long duration = sample.stop(timer);
		long endMemory = getUsedMemory();
		long memoryUsed = Math.max(0, endMemory - startMemory); // 음수 방지

		log.info("Upload Performance - Method: {}, Size: {}, Time: {} ms, Memory: {} MB",
			method,
			formatFileSize(fileSize),
			duration / 1_000_000,
			memoryUsed / (1024 * 1024));

		// 메모리 사용량 기록
		Gauge.builder("file.upload.memory", () -> memoryUsed)
			.tag("method", method)
			.tag("size", formatFileSize(fileSize))
			.register(meterRegistry);

		// 결과가 UploadResult인 경우 메모리 사용량 설정
		if (result instanceof UploadResult) {
			((UploadResult)result).setMemoryUsedBytes(memoryUsed);
		}

		return result;
	}

	private long getUsedMemory() {
		Runtime runtime = Runtime.getRuntime();
		return runtime.totalMemory() - runtime.freeMemory();
	}

	private String formatFileSize(long size) {
		if (size < 1024 * 1024) {
			return Math.round(size / 1024.0) + "KB";
		} else if (size < 1024 * 1024 * 1024) {
			return Math.round(size / (1024.0 * 1024.0)) + "MB";
		} else {
			return Math.round(size / (1024.0 * 1024.0 * 1024.0)) + "GB";
		}
	}
}
