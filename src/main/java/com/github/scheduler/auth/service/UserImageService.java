package com.github.scheduler.auth.service;

import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.entity.UserImageEntity;
import com.github.scheduler.auth.repository.UserImageRepository;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItem;
import org.mockito.Mock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserImageService {

    private final UserImageRepository userImageRepository;

    // 이미지 업로드
    public void uploadUserImage(UserEntity userEntity, MultipartFile image, boolean isDefaultImage) {
        try {
            String dbFilePath;

            if (isDefaultImage) {
                dbFilePath = "/uploads/profiles/base.png"; // 기본 이미지 경로 그대로 사용
            } else {
                String uploadDir = "src/main/resources/static/uploads/profiles/";
                dbFilePath = saveImage(image, uploadDir); // 업로드된 이미지만 저장
            }

            UserImageEntity userImageEntity = new UserImageEntity(userEntity, dbFilePath);
            userImageRepository.save(userImageEntity);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 기본 이미지 불러오기
    public MultipartFile getDefaultProfileImage() throws IOException {
        ClassPathResource defaultImageResource = new ClassPathResource("static/uploads/profiles/base.png");

        try (InputStream inputStream = defaultImageResource.getInputStream()) {
            return new MockMultipartFile(
                    "base.png",
                    "base.png",
                    MediaType.IMAGE_PNG_VALUE,
                    inputStream
            );
        }
    }

    // 이미지 파일을 저장
    public String saveImage(MultipartFile image, String uploadsDir) throws IOException {
        // 파일 이름 생성
        String fileName = UUID.randomUUID().toString().replace("-", "") + "_" + image.getOriginalFilename();
        // 실제 파일이 저장될 경로
        String filePath = uploadsDir + fileName;
        // DB에 저장할 경로 문자열
        String dbFilePath = "/uploads/profiles/" + fileName;

        Path path = Paths.get(filePath); // Path 객체 생성
        Files.createDirectories(path.getParent()); // 디렉토리 생성
        Files.write(path, image.getBytes()); // 디렉토리에 파일 저장

        return dbFilePath;
    }
}
