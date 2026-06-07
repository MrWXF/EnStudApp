package com.enstud.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("enstud_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名 */
    private String username;

    /** 邮箱 */
    private String email;

    /** 密码哈希（BCrypt） */
    private String passwordHash;

    /** 头像 URL */
    private String avatarUrl;

    /** 昵称 */
    private String nickname;

    /** 用户等级 */
    private Integer level;

    /** 积分 */
    private Integer points;

    /** 最后登录时间 */
    private LocalDateTime lastLoginAt;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
