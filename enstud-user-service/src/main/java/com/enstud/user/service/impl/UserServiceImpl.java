package com.enstud.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enstud.common.BusinessException;
import com.enstud.common.JwtUtil;
import com.enstud.common.constant.ErrorCode;
import com.enstud.common.entity.User;
import com.enstud.user.dto.LoginRequest;
import com.enstud.user.dto.LoginResultDTO;
import com.enstud.user.dto.RegisterRequest;
import com.enstud.user.dto.UserProfileDTO;
import com.enstud.user.mapper.UserMapper;
import com.enstud.user.service.LearningStreakService;
import com.enstud.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final LearningStreakService learningStreakService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest request) {
        // 检查用户名是否已存在
        Long usernameCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.username()));
        if (usernameCount > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        // 检查邮箱是否已注册
        Long emailCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getEmail, request.email()));
        if (emailCount > 0) {
            throw new BusinessException(ErrorCode.EMAIL_REGISTERED);
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEmail(request.email());
        user.setNickname(request.nickname() != null ? request.nickname() : request.username());
        user.setLevel(1);
        user.setPoints(0);

        userMapper.insert(user);
        log.info("用户注册成功, userId={}, username={}", user.getId(), user.getUsername());
    }

    @Override
    public LoginResultDTO login(LoginRequest request) {
        // 查找用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.username()));
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 验证密码
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 更新连续学习天数（每天首次登录时触发）
        learningStreakService.updateStreak(user.getId());

        // 生成 Token
        String accessToken = JwtUtil.generateAccessToken(user.getId(), user.getUsername(), jwtSecret);
        String refreshToken = JwtUtil.generateRefreshToken(user.getId(), jwtSecret);

        log.info("用户登录成功, userId={}, username={}", user.getId(), user.getUsername());

        return new LoginResultDTO(accessToken, refreshToken,
                user.getId(), user.getUsername(), user.getNickname());
    }

    @Override
    public String getUserName(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) return null;
        return user.getNickname() != null ? user.getNickname() : user.getUsername();
    }

    @Override
    public UserProfileDTO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        return new UserProfileDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getNickname(),
                user.getAvatarUrl(),
                user.getLevel(),
                user.getPoints(),
                user.getLastLoginAt(),
                user.getCreatedAt()
        );
    }
}
