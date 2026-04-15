package com.nutrivista.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI nutriVistaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NutriVista API")
                        .description("""
                                **NutriVista** — 个人饮食营养大数据可视化分析系统

                                提供食物营养数据查询、饮食记录管理、营养摄入统计分析等功能。

                                - **食物模块** `/api/foods`：8000+ 种食物的营养数据检索
                                - **饮食记录** `/api/meals`：每日三餐及加餐记录管理
                                - **统计分析** `/api/stats`：多维度营养摄入可视化数据
                                - **AI 扩展** `/api/ai`：预留 AI 饮食分析接口
                                """)
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("NutriVista Team")
                                .email("support@nutrivista.com"))
                        .license(new License()
                                .name("MIT License")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("本地开发环境"),
                        new Server().url("https://api.nutrivista.com").description("生产环境（预留）")
                ));
    }
}
