package com.github.scheduler.oauth2.service;

import com.github.scheduler.auth.entity.Role;
import com.github.scheduler.auth.entity.UserEntity;
import com.github.scheduler.auth.repository.UserImageRepository;
import com.github.scheduler.auth.repository.UserRepository;
import com.github.scheduler.auth.service.UserImageService;
import com.github.scheduler.global.config.auth.custom.CustomUserDetails;
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
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserImageService userImageService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        OAuth2UserInfo oAuth2UserInfo = null;

        String provider = userRequest.getClientRegistration().getRegistrationId(); // google, kakao 등

        if (provider.equals("google")) {
            oAuth2UserInfo = new GoogleUserInfo(oauth2User.getAttributes());
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

            UserEntity managedUser = userRepository.findByUserId(savedUser.getUserId())
                    .orElseThrow(() -> new RuntimeException("유저 저장 실패 후 조회 불가"));

            try {
                String imageUrl = oAuth2UserInfo.getImage();
                boolean isDefaultImage = (imageUrl == null || imageUrl.isBlank());

                if (isDefaultImage) {
                    MultipartFile image = userImageService.getDefaultProfileImage();
                    userImageService.uploadUserImage(managedUser, image, true);
                } else {
                    userImageService.uploadUserImageFromUrl(managedUser, imageUrl);
                }

            } catch (Exception e) {
                log.error("프로필 이미지 저장 실패", e);
            }
        }

        return new CustomUserDetails(oauthUser, oauth2User.getAttributes());
    }
}
