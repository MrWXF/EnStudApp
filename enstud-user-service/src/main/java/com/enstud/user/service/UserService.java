package com.enstud.user.service;

import com.enstud.user.dto.LoginRequest;
import com.enstud.user.dto.LoginResultDTO;
import com.enstud.user.dto.RegisterRequest;
import com.enstud.user.dto.UserProfileDTO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    void register(RegisterRequest request);

    /**
     * 用户登录
     */
    LoginResultDTO login(LoginRequest request);

    /**
     * 获取用户个人信息
     */
    UserProfileDTO getProfile(Long userId);

    /**
     * 根据 userId 获取用户昵称（优先）或用户名
     *
     * @param userId 用户ID
     * @return 昵称或用户名，用户不存在返回 null
     */
    String getUserName(Long userId);
}
