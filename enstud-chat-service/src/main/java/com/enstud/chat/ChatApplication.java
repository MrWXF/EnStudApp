package com.enstud.chat;

import com.enstud.common.GlobalExceptionHandler;
import com.enstud.common.config.MybatisPlusConfig;
import com.enstud.common.config.WebMvcConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = {"com.enstud.chat", "com.enstud.translate", "com.enstud.common"}, exclude = RedissonAutoConfigurationV2.class)
@Import({GlobalExceptionHandler.class, MybatisPlusConfig.class, WebMvcConfig.class})
@MapperScan("com.enstud.chat.mapper")
@EnableFeignClients(basePackages = "com.enstud.common.feign")
public class ChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }
}