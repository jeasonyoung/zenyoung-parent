package top.zenyoung.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import top.zenyoung.boot.util.RespJsonUtils;
import top.zenyoung.security.auth.SecurityAuthenticationManager;
import top.zenyoung.security.filter.JwtTokenFilter;

/**
 * 安全配置
 *
 * @author young
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired(required = false)
    private SecurityAuthenticationManager manager;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .antMatchers(manager.getWhiteUrls()).permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(new JwtTokenFilter(manager, objectMapper), UsernamePasswordAuthenticationFilter.class)
                .cors()
                .and()
                .formLogin().disable()
                .httpBasic().disable()
                .csrf().disable()
                .exceptionHandling(ehc -> {
                    //未登录处理
                    ehc.authenticationEntryPoint((req, res, e) -> RespJsonUtils.buildFailResp(objectMapper, res, HttpStatus.UNAUTHORIZED, e));
                    //访问拒绝处理
                    ehc.accessDeniedHandler((req, res, e) -> RespJsonUtils.buildFailResp(objectMapper, res, HttpStatus.FORBIDDEN, e));
                });
    }
}
