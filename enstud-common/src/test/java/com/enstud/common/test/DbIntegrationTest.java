package com.enstud.common.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 数据库集成测试基类
 * <p>
 * 继承 BaseIntegrationTest，添加 JdbcTemplate 用于直接查询 DB 验证结果。
 * 适用于有数据库依赖的服务（user/word/chat/writing/forum/read）。
 */
public abstract class DbIntegrationTest extends BaseIntegrationTest {

    @Autowired
    protected JdbcTemplate jdbcTemplate;
}
