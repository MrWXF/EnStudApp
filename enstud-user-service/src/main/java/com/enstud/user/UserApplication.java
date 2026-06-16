package com.enstud.user;

import com.enstud.common.GlobalExceptionHandler;
import com.enstud.common.config.MybatisPlusConfig;
import com.enstud.common.config.WebMvcConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude = RedissonAutoConfigurationV2.class)
@EnableFeignClients
@Import({GlobalExceptionHandler.class, MybatisPlusConfig.class, WebMvcConfig.class})
@MapperScan("com.enstud.user.mapper")
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
