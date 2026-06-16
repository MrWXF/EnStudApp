package com.enstud.translate;

import com.enstud.common.GlobalExceptionHandler;
import com.enstud.common.config.WebMvcConfig;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, RedissonAutoConfigurationV2.class})
@Import({GlobalExceptionHandler.class, WebMvcConfig.class})
public class TranslateApplication {
    public static void main(String[] args) { SpringApplication.run(TranslateApplication.class, args); }
}
