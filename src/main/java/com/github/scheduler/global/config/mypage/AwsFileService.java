package com.github.scheduler.global.config.mypage;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.github.scheduler.global.exception.AppException;
import com.github.scheduler.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AwsFileService {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final String IMAGE_DIR = "profiles/";  // 프로필 이미지 저장 폴더

    // 파일 저장 메서드
    public String savePhoto(MultipartFile multipartFile, Long memberId) {
        try {
            File uploadFile = convert(multipartFile)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_SAVE_FILE,ErrorCode.NOT_SAVE_FILE.getMessage()));
            return upload(uploadFile, IMAGE_DIR, memberId);
        } catch (IOException e) {
            throw new AppException(ErrorCode.NOT_SAVE_FILE,ErrorCode.NOT_SAVE_FILE.getMessage());
        }
    }

    // S3로 파일 업로드하기
    private String upload(File uploadFile, String dirName, Long memberId) {
        String fileName = dirName + memberId + "/" + UUID.randomUUID() + uploadFile.getName();  // S3에 저장된 파일 이름
        try {
            String uploadImageUrl = putS3(uploadFile, fileName);  // s3로 업로드
            removeNewFile(uploadFile);
            return uploadImageUrl;
        } catch (Exception e) {
            throw new AppException(ErrorCode.NOT_SAVE_FILE,ErrorCode.NOT_SAVE_FILE.getMessage());
        }
    }

    // S3로 업로드
    private String putS3(File uploadFile, String fileName) {
        try {
            amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
            System.out.println(uploadFile);
            return amazonS3Client.getUrl(bucket, fileName).toString();
        } catch (Exception e) {
            throw new AppException(ErrorCode.NOT_SAVE_FILE,ErrorCode.NOT_SAVE_FILE.getMessage());
        }
    }

    // S3에서 파일 삭제
    public void deletePhoto(String imageUrl) {
        try {
            // URL에서 파일 경로 추출
            String fileName = imageUrl.replace("https://"+bucket+".s3.amazonaws.com/", "");
            log.info("File to be deleted: " + fileName);

            // S3에서 해당 파일 삭제
            amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, fileName));
            log.info("S3 file deleted successfully: " + imageUrl);
        } catch (Exception e) {
            log.error("Error occurred while deleting file from S3: " + imageUrl, e);
        }
    }

    // 로컬에 저장된 이미지 지우기
    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.info("File delete success");
            return;
        }
        log.info("File delete fail");
    }

    // 로컬에 파일 업로드 하기
    private Optional<File> convert(MultipartFile file) throws IOException {
        String uploadDir = new File("src/main/resources/static/upload").getAbsolutePath();
        File directory = new File(uploadDir);

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("디렉토리 생성 실패: " + directory.getAbsolutePath());
        }

        File convertFile = new File(directory, Objects.requireNonNull(file.getOriginalFilename()));

        if (convertFile.exists()) {
            convertFile.delete(); // 기존 파일 삭제
        }

        if (convertFile.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            }
            return Optional.of(convertFile);
        }
        return Optional.empty();
    }
}