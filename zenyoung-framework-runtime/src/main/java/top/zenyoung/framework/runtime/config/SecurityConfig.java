package top.zenyoung.framework.runtime.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import top.zenyoung.framework.auth.BaseAuthenticationManagerService;
import top.zenyoung.security.webmvc.filter.JwtLoginFilter;
import top.zenyoung.security.webmvc.filter.JwtTokenFilter;

/**
 * 安全配置
 *
 * @author young
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private ApplicationContext context;

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        final BaseAuthenticationManagerService manager = context.getBean(BaseAuthenticationManagerService.class);
        http.authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .antMatchers(manager.getWhiteUrls()).permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(new JwtTokenFilter(manager), UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(new JwtLoginFilter<>(manager), UsernamePasswordAuthenticationFilter.class)
                .cors()
                .and()
                .formLogin().disable()
                .httpBasic().disable()
                .csrf().disable()
                .exceptionHandling(ehc -> {
                    //未登录处理
                    ehc.authenticationEntryPoint((req, res, e) -> manager.unsuccessfulAuthentication(res, HttpStatus.UNAUTHORIZED, e));
                    //访问拒绝处理
                    ehc.accessDeniedHandler((req, res, e) -> manager.unsuccessfulAuthentication(res, HttpStatus.FORBIDDEN, e));
                });
    }
}
