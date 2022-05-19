package top.zenyoung.framework.runtime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import top.zenyoung.common.sequence.IdSequence;
import top.zenyoung.common.sequence.SnowFlake;
import top.zenyoung.framework.auth.AuthProperties;
import top.zenyoung.framework.auth.BaseAuthenticationManagerService;
import top.zenyoung.framework.runtime.config.RuntimeProperties;
import top.zenyoung.security.webmvc.filter.JwtLoginFilter;
import top.zenyoung.security.webmvc.filter.JwtTokenFilter;

/**
 * 运行时模块-自动配置
 *
 * @author young
 */
@Configuration
@ConditionalOnClass(RuntimeProperties.class)
@EnableConfigurationProperties(RuntimeProperties.class)
@ConditionalOnProperty(prefix = "top.zenyoung.runtime", value = "enable", matchIfMissing = true)
public class RuntimeAutoConfiguration {
    @Autowired
    private RuntimeProperties properties;
    @Autowired
    private ApplicationContext context;

    @Bean
    @ConditionalOnMissingBean(IdSequence.class)
    private IdSequence buildSequence() {
        final int max = 10;
        final int cpus = Math.max(Runtime.getRuntime().availableProcessors(), 1);
        return SnowFlake.getInstance(cpus & max, (cpus * 2) & max);
    }

    @Bean
    @ConditionalOnMissingBean(AuthProperties.class)
    public AuthProperties getAuthConfig() {
        return properties.getAuth();
    }

    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean(WebSecurityConfigurerAdapter.class)
    public WebSecurityConfigurerAdapter webSecurityConfigurerAdapter() {
        return new WebSecurityConfigurerAdapter() {
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
        };
    }

}
