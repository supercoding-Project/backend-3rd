package com.github.scheduler.auth.service;

import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.entity.UserImageEntity;
import com.github.scheduler.auth.repository.UserImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserImageService {

    private final UserImageRepository userImageRepository;

    public void uploadUserImage(UserEntity userEntity, MultipartFile image) {
        try {
            // 프로필 이미지 저장 경로
            String uploadDir = "src/main/resources/static/uploads/profiles/";
            String dbFilePath = saveImage(image, uploadDir);

            UserImageEntity userImageEntity = new UserImageEntity(userEntity, dbFilePath);
            userImageRepository.save(userImageEntity);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
