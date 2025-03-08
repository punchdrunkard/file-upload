package com.example.fileupload.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

	@Value("${app.upload.dir:uploads}")
	private String uploadDir;

	public String saveMultipartFile(MultipartFile file, String subDir) throws IOException {
		String filename = StringUtils.cleanPath(file.getOriginalFilename());
		Path uploadPath = Paths.get(uploadDir, subDir);

		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}

		Path filePath = uploadPath.resolve(filename);
		Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

		return filePath.toString();
	}

	public String saveInputStream(InputStream inputStream, String filename, String subDir) throws IOException {
		Path uploadPath = Paths.get(uploadDir, subDir);

		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}

		Path filePath = uploadPath.resolve(filename);

		try (FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
			byte[] buffer = new byte[8192];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
		}

		return filePath.toString();
	}
}
