package com.enstud.forum;

import com.enstud.common.GlobalExceptionHandler;
import com.enstud.common.config.MybatisPlusConfig;
import com.enstud.common.config.WebMvcConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({GlobalExceptionHandler.class, MybatisPlusConfig.class, WebMvcConfig.class})
@MapperScan("com.enstud.forum.mapper")
public class ForumApplication {
    public static void main(String[] args) { SpringApplication.run(ForumApplication.class, args); }
}
