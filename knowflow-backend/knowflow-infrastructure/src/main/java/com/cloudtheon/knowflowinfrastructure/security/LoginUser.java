package com.cloudtheon.knowflowinfrastructure.security;

import lombok.Getter;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;

/**
 * 自定义登录用户（携带 userId）
 */
@Getter
public class LoginUser extends User {

    private final Long userId;

    public LoginUser(Long userId, String username, String password) {
        super(username, password, Collections.emptyList());
        this.userId = userId;
    }
}
