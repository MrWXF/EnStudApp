package com.enstud.chat;

import com.enstud.common.GlobalExceptionHandler;
import com.enstud.common.config.MybatisPlusConfig;
import com.enstud.common.config.WebMvcConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude = RedissonAutoConfigurationV2.class)
@Import({GlobalExceptionHandler.class, MybatisPlusConfig.class, WebMvcConfig.class})
@MapperScan("com.enstud.chat.mapper")
public class ChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }
}
