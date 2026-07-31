package com.cloudtheon.knowflowweb.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger 文档配置。
 * <p>
 * 访问地址：
 * <ul>
 *   <li>Swagger UI：<a href="http://localhost:8080/swagger-ui.html">/swagger-ui.html</a></li>
 *   <li>OpenAPI JSON：<a href="http://localhost:8080/api-docs">/api-docs</a></li>
 *   <li>OpenAPI YAML：<a href="http://localhost:8080/api-docs.yaml">/api-docs.yaml</a></li>
 * </ul>
 * </p>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI knowflowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("KnowFlow — 智能学习助手平台 API")
                        .description("""
                                KnowFlow 是一个基于 Spring AI 2.0 与 RAG（检索增强生成）技术的全栈智能学习助手平台。
                                
                                ### 核心功能
                                - **用户管理**：注册、登录、个人信息获取
                                - **智能对话**：多轮连续对话、SSE 流式输出、对话历史管理
                                - **RAG 知识库**：文档上传解析、基于知识库的精准问答、文档管理
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("KnowFlow Team")
                                .url("https://github.com/cloudtheon/knowflow"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/api")
                                .description("本地开发环境"),
                        new Server()
                                .url("https://api.knowflow.dev/api")
                                .description("生产环境")
                ));
    }
}
