package com.enstud.forum;

import com.enstud.common.GlobalExceptionHandler;
import com.enstud.common.config.MybatisPlusConfig;
import com.enstud.common.config.WebMvcConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = {"com.enstud.forum", "com.enstud.word", "com.enstud.read", "com.enstud.writing", "com.enstud.common"}, exclude = RedissonAutoConfigurationV2.class)
@Import({GlobalExceptionHandler.class, MybatisPlusConfig.class, WebMvcConfig.class})
@MapperScan({"com.enstud.forum.mapper", "com.enstud.word.mapper", "com.enstud.read.mapper", "com.enstud.writing.mapper"})
@EnableFeignClients(basePackages = "com.enstud.common.feign")
public class ForumApplication {
    public static void main(String[] args) {
        SpringApplication.run(ForumApplication.class, args);
    }
}