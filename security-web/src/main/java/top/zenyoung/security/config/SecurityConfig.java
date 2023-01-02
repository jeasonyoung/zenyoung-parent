package top.zenyoung.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import top.zenyoung.boot.util.RespJsonUtils;
import top.zenyoung.security.filter.JwtTokenFilter;
import top.zenyoung.security.service.AuthenManagerService;

import java.util.Objects;

/**
 * 安全配置
 *
 * @author young
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    @Autowired(required = false)
    private AuthenManagerService manager;

    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        if (Objects.isNull(manager)) {
            log.error("未配置安全认证管理器服务接口: {}", AuthenManagerService.class);
            return null;
        }
        return http.authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .antMatchers(manager.getWhiteUrls()).permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(new JwtTokenFilter(manager, objectMapper), UsernamePasswordAuthenticationFilter.class)
                .cors().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .csrf().disable()
                .exceptionHandling(ehc -> {
                    //未登录处理
                    ehc.authenticationEntryPoint((req, res, e) -> RespJsonUtils.buildFailResp(objectMapper, res, HttpStatus.UNAUTHORIZED, e));
                    //访问拒绝处理
                    ehc.accessDeniedHandler((req, res, e) -> RespJsonUtils.buildFailResp(objectMapper, res, HttpStatus.FORBIDDEN, e));
                })
                .build();
    }
}
