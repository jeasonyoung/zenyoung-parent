package top.zenyoung.security.auth;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.CollectionUtils;
import top.zenyoung.boot.config.CaptchaProperties;
import top.zenyoung.boot.service.CaptchaService;
import top.zenyoung.common.model.Status;
import top.zenyoung.common.model.UserPrincipal;
import top.zenyoung.security.config.SecurityProperties;
import top.zenyoung.security.dto.AuthUserDTO;
import top.zenyoung.security.dto.LoginBodyDTO;
import top.zenyoung.security.exception.TokenException;
import top.zenyoung.security.token.TokenService;
import top.zenyoung.security.token.TokenVerifyService;
import top.zenyoung.security.vo.LoginBodyVO;

import javax.annotation.Nonnull;
import javax.security.auth.login.AccountException;
import javax.security.auth.login.AccountNotFoundException;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 令牌认证管理器-基类
 *
 * @author young
 */
@Slf4j
public class BaseSecurityAuthenticationManager implements SecurityAuthenticationManager {
    @Autowired
    private SecurityProperties properties;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private TokenVerifyService tokenVerifyService;

    @Autowired
    private AuthenService authenService;

    @Autowired(required = false)
    private CaptchaService captchaService;

    @Override
    public String[] getWhiteUrls() {
        final List<String> whiteUrls;
        if (!CollectionUtils.isEmpty(whiteUrls = this.properties.getWhiteUrls())) {
            return whiteUrls.stream()
                    .filter(url -> !Strings.isNullOrEmpty(url))
                    .toArray(String[]::new);
        }
        return new String[0];
    }

    @Override
    public String[] getLoginUrls() {
        final List<String> urls;
        if (!CollectionUtils.isEmpty(urls = this.properties.getLoginUrls())) {
            return urls.stream()
                    .filter(url -> !Strings.isNullOrEmpty(url))
                    .toArray(String[]::new);
        }
        return new String[0];
    }

    /**
     * 用户认证
     *
     * @param dto          登录信息
     * @param freePassword 是否免费
     * @return 认证结果
     * @throws AccountException 账号异常
     */
    public LoginBodyVO authen(@Nonnull final LoginBodyDTO dto, @Nonnull final Boolean freePassword) throws AccountException {
        if (!freePassword) {
            //检查账号/密码
            if (Strings.isNullOrEmpty(dto.getAccount()) || Strings.isNullOrEmpty(dto.getPasswd())) {
                throw new AccountNotFoundException("账号或密码不能为空");
            }
            //检查验证码
            verifyCaptcha(dto);
        }
        //获取账号数据
        final AuthUserDTO auth = this.authenService.findByAccount(dto.getAccount());
        if (Objects.isNull(auth)) {
            throw new AccountNotFoundException("账号不存在!");
        }
        //验证密码
        if (!freePassword) {
            //检查账号
            if (auth.getStatus() != Status.Enable) {
                throw new LockedException("账号已被锁定");
            }
            //校验密码
            if (!passwordEncoder.matches(dto.getPasswd(), auth.getPassword())) {
                throw new BadCredentialsException("账号或密码错误");
            }
        }
        //创建登录令牌数据
        final Ticket ticket = new Ticket(new UserPrincipal(auth.getId() + "", auth.getAccount(), auth.getRoles(), dto.getDevice()));
        final Token token = tokenService.createToken(ticket);
        return LoginBodyVO.builder()
                .accessToken(token.getAccessToken())
                .refershToken(token.getRefershToken())
                .user(ticket.toClaims())
                .build();
    }

    /**
     * 图形验证码验证
     *
     * @param dto 请求数据
     */
    protected void verifyCaptcha(@Nonnull final LoginBodyDTO dto) {
        //校验验证码
        final CaptchaProperties captchaProperties;
        if (Objects.nonNull(captchaService) && Objects.nonNull(captchaProperties = properties.getCaptcha()) && captchaProperties.getEnable()) {
            //检查验证码ID及用户输入验证码
            final Long captchaId = dto.getVerifyId();
            final String inputCaptchaCode = dto.getVerifyCode();
            if (Objects.isNull(captchaId) || Strings.isNullOrEmpty(inputCaptchaCode)) {
                throw new BadCredentialsException("请输入验证码");
            }
            //校验图形验证码
            if (!captchaService.verify(captchaId, inputCaptchaCode)) {
                throw new BadCredentialsException("验证码不正确");
            }
        }
    }

    /**
     * 解析用户认证令牌
     *
     * @param request 请求对象
     * @return 用户认证令牌
     */
    @Override
    public Authentication parseAuthenticationToken(@Nonnull final HttpServletRequest request) throws TokenException {
        final String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!Strings.isNullOrEmpty(token)) {
            String tokenVal = token.trim();
            //检查是否有Bearer
            final String bearer = "Bearer ";
            if (token.startsWith(bearer)) {
                tokenVal = token.replaceFirst(bearer, "").trim();
            }
            final Ticket ticket = this.tokenVerifyService.checkToken(tokenVal);
            if (ticket == null) {
                throw new TokenException("令牌无效!");
            }
            final List<String> roles = ticket.getRoles();
            final List<? extends GrantedAuthority> authorities = CollectionUtils.isEmpty(roles) ? Lists.newArrayList() :
                    roles.stream()
                            .filter(role -> !Strings.isNullOrEmpty(role))
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
            return new UsernamePasswordAuthenticationToken(ticket, null, authorities);
        }
        return null;
    }
}
