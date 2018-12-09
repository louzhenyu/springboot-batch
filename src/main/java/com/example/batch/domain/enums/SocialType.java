package com.example.batch.domain.enums;

/**
 * Created by gavinkim at 2018-12-09
 */
public enum SocialType {

    FACEBOOK("facebook"),
    GOOGLE("google"),
    KAKAO("kakao");

    private final String ROLE_PREFIX = "ROLE_";
    private String name;

    SocialType(String name) {
        this.name = name;
    }
    public String getRoleType() { return ROLE_PREFIX + name.toUpperCase(); }
    public String getValue() { return name; }
    public boolean isEquals(String authority) {
        return this.getRoleType().equals(authority);
    }
}
