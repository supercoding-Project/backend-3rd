package com.github.scheduler.oauth2.service;

import com.github.scheduler.auth.entity.Role;
import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.repository.UserImageRepository;
import com.github.scheduler.auth.repository.UserRepository;
import com.github.scheduler.auth.service.UserImageService;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
import com.github.scheduler.global.util.MultipartFileConverter;
import com.github.scheduler.oauth2.info.GoogleUserInfo;
import com.github.scheduler.oauth2.info.KakaoUserInfo;
import com.github.scheduler.oauth2.info.NaverUserInfo;
import com.github.scheduler.oauth2.info.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserImageRepository userImageRepository;
    private final UserImageService userImageService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        OAuth2UserInfo oAuth2UserInfo = null;

        String provider = userRequest.getClientRegistration().getRegistrationId(); // google, kakao 등

        if (provider.equals("google")) {
            oAuth2UserInfo = new GoogleUserInfo(oauth2User.getAttributes());
            log.info("Google OAuth attributes: {}", oauth2User.getAttributes());
        } else if (provider.equals("kakao")) {
            oAuth2UserInfo = new KakaoUserInfo(oauth2User.getAttributes());
        } else if (provider.equals("naver")) {
            oAuth2UserInfo = new NaverUserInfo(oauth2User.getAttributes());
        }

        log.info("OAuth2 User Attributes: {}", oauth2User.getAttributes());

        String email = oAuth2UserInfo.getEmail();
        if (email == null || email.isBlank()) {
            email = provider + "_" + oAuth2UserInfo.getProviderId() + "@socialuser.com";
        }
        Optional<UserEntity> existingUser = userRepository.findByEmail(email);
        UserEntity oauthUser;

        if (existingUser.isPresent()) {
            oauthUser = existingUser.get();
        } else {
            oauthUser = UserEntity.builder()
                    .email(email)
                    .password("OAuth2")
                    .username(oAuth2UserInfo.getName())
                    .phone(null)
                    .provider(provider)
                    .providerId(oAuth2UserInfo.getProviderId())
                    .role(Role.ROLE_USER)
                    .createdAt(LocalDateTime.now())
                    .build();

            UserEntity savedUser = userRepository.save(oauthUser);

            try {
                String imageUrl = oAuth2UserInfo.getImage();
                boolean isDefaultImage = (imageUrl == null || imageUrl.isBlank());
                MultipartFile image = isDefaultImage
                        ? userImageService.getDefaultProfileImage()
                        : convertUrlToMultipartFile(imageUrl);

                userImageService.uploadUserImage(savedUser, image, isDefaultImage);
            } catch (Exception e) {
                log.error("프로필 이미지 저장 실패", e);
            }
        }

        return new CustomUserDetails(oauthUser, oauth2User.getAttributes());
    }

    private MultipartFile convertUrlToMultipartFile(String imageUrl) throws IOException {
        log.info("imageUrl: {}", imageUrl);

        // URL에서 프로토콜 부분 "https://" 제거
        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);  // URL 마지막 부분 가져오기

        // URL에서 ':' 등의 특수 문자는 파일명에 사용할 수 없으므로, '_'로 변경
        fileName = fileName.replaceAll("[^a-zA-Z0-9.-]", "_");

        // 저장할 디렉토리 경로
        String directory = "src/main/resources/static/uploads/profiles/";

        // 전체 파일 경로
        Path path = Paths.get(directory, fileName);

        // URL에서 이미지를 읽어온 후 파일로 변환
        URL url = new URL(imageUrl);
        try (InputStream inputStream = url.openStream(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            BufferedImage urlImage = ImageIO.read(inputStream);
            ImageIO.write(urlImage, "jpg", bos);  // 이미지를 byte 배열로 변환
            byte[] byteArray = bos.toByteArray();

            // MultipartFile로 변환하여 반환
            return new MultipartFileConverter(byteArray, path.toString());
        }
    }
}
