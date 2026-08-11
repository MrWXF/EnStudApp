package com.enstud.read;

import com.enstud.common.GlobalExceptionHandler;
import com.enstud.common.config.WebMvcConfig;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = RedissonAutoConfigurationV2.class)
@EnableScheduling
@EnableFeignClients(basePackages = "com.enstud.common.feign")
@Import({GlobalExceptionHandler.class, WebMvcConfig.class})
public class ReadApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReadApplication.class, args);
    }
}
