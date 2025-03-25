package com.github.scheduler.oauth2.info;

import lombok.Data;

import java.util.Map;

@Data
public class NaverUserInfo implements OAuth2UserInfo {
    private Map<String, Object> attributes; // 전체 응답
    private Map<String, Object> response;   // 실제 유저 정보가 담긴 부분

    @SuppressWarnings("unchecked")
    public NaverUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.response = (Map<String, Object>) attributes.get("response");
    }

    @Override
    public String getProviderId() {
        return (String) response.get("id");
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getEmail() {
        return (String) response.get("email");
    }

    @Override
    public String getName() {
        return (String) response.get("name"); // 또는 nickname
    }

    @Override
    public String getPhoneNumber() {
        return (String) response.get("mobile");
    }

    @Override
    public String getImage() {
        return (String) response.get("profile_image");
    }
}
