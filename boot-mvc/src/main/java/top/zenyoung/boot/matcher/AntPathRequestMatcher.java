package top.zenyoung.boot.matcher;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UrlPathHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

/**
 * AntPathRequestMatcher
 *
 * @author young
 */
public class AntPathRequestMatcher implements RequestMatcher {
    private static final String MATCH_ALL = "/**";
    private final Matcher matcher;
    @Getter
    private final String pattern;
    private final HttpMethod httpMethod;
    private final UrlPathHelper urlPathHelper;

    /**
     * Creates a matcher with the specific pattern which will match all HTTP methods in a
     * case sensitive manner.
     *
     * @param pattern the ant pattern to use for matching
     */
    public AntPathRequestMatcher(@Nonnull final String pattern) {
        this(pattern, null);
    }

    /**
     * Creates a matcher with the supplied pattern and HTTP method in a case sensitive
     * manner.
     *
     * @param pattern    the ant pattern to use for matching
     * @param httpMethod the HTTP method. The {@code matches} method will return false if
     *                   the incoming request doesn't have the same method.
     */
    public AntPathRequestMatcher(@Nonnull final String pattern, @Nullable final String httpMethod) {
        this(pattern, httpMethod, true);
    }

    /**
     * Creates a matcher with the supplied pattern which will match the specified Http
     * method
     *
     * @param pattern       the ant pattern to use for matching
     * @param httpMethod    the HTTP method. The {@code matches} method will return false if
     *                      the incoming request doesn't doesn't have the same method.
     * @param caseSensitive true if the matcher should consider case, else false
     */
    public AntPathRequestMatcher(@Nonnull final String pattern, @Nullable final String httpMethod, final boolean caseSensitive) {
        this(pattern, httpMethod, caseSensitive, null);
    }

    /**
     * Creates a matcher with the supplied pattern which will match the specified Http
     * method
     *
     * @param pattern       the ant pattern to use for matching
     * @param httpMethod    the HTTP method. The {@code matches} method will return false if
     *                      the incoming request doesn't have the same method.
     * @param caseSensitive true if the matcher should consider case, else false
     * @param urlPathHelper if non-null, will be used for extracting the path from the
     *                      HttpServletRequest
     */
    public AntPathRequestMatcher(String pattern, final String httpMethod, final boolean caseSensitive,
                                 @Nullable final UrlPathHelper urlPathHelper) {
        Assert.hasText(pattern, "Pattern cannot be null or empty");
        if (pattern.equals(MATCH_ALL) || "**".equals(pattern)) {
            pattern = MATCH_ALL;
            this.matcher = null;
        } else {
            // If the pattern ends with {@code /**} and has no other wildcards or path
            // variables, then optimize to a sub-path match
            if (pattern.endsWith(MATCH_ALL)
                    && (pattern.indexOf('?') == -1 && pattern.indexOf('{') == -1 && pattern.indexOf('}') == -1)
                    && pattern.indexOf("*") == pattern.length() - 2) {
                this.matcher = new SubpathMatcher(pattern.substring(0, pattern.length() - 3), caseSensitive);
            } else {
                this.matcher = new SpringAntMatcher(pattern, caseSensitive);
            }
        }
        this.pattern = pattern;
        this.httpMethod = StringUtils.hasText(httpMethod) ? HttpMethod.valueOf(httpMethod) : null;
        this.urlPathHelper = urlPathHelper;
    }

    /**
     * Returns true if the configured pattern (and HTTP-Method) match those of the
     * supplied request.
     *
     * @param request the request to match against. The ant pattern will be matched
     *                against the {@code servletPath} + {@code pathInfo} of the request.
     */
    @Override
    public boolean matches(@Nonnull final HttpServletRequest request) {
        if (this.httpMethod != null
                && StringUtils.hasText(request.getMethod())
                && this.httpMethod != HttpMethod.valueOf(request.getMethod())) {
            return false;
        }
        if (this.pattern.equals(MATCH_ALL)) {
            return true;
        }
        final String url = getRequestPath(request);
        return this.matcher.matches(url);
    }

    @Override
    public MatchResult matcher(@Nonnull final HttpServletRequest request) {
        if (!matches(request)) {
            return MatchResult.notMatch();
        }
        if (this.matcher == null) {
            return MatchResult.match();
        }
        final String url = getRequestPath(request);
        return MatchResult.match(this.matcher.extractUriTemplateVariables(url));
    }

    private String getRequestPath(@Nonnull final HttpServletRequest request) {
        if (this.urlPathHelper != null) {
            return this.urlPathHelper.getPathWithinApplication(request);
        }
        String url = request.getServletPath();
        final String pathInfo = request.getPathInfo();
        if (pathInfo != null) {
            url = StringUtils.hasLength(url) ? url + pathInfo : pathInfo;
        }
        return url;
    }

    private interface Matcher {

        boolean matches(String path);

        Map<String, String> extractUriTemplateVariables(String path);

    }

    private static final class SpringAntMatcher implements Matcher {

        private final AntPathMatcher antMatcher;

        private final String pattern;

        private SpringAntMatcher(String pattern, boolean caseSensitive) {
            this.pattern = pattern;
            this.antMatcher = createMatcher(caseSensitive);
        }

        @Override
        public boolean matches(String path) {
            return this.antMatcher.match(this.pattern, path);
        }

        @Override
        public Map<String, String> extractUriTemplateVariables(String path) {
            return this.antMatcher.extractUriTemplateVariables(this.pattern, path);
        }

        private static AntPathMatcher createMatcher(boolean caseSensitive) {
            AntPathMatcher matcher = new AntPathMatcher();
            matcher.setTrimTokens(false);
            matcher.setCaseSensitive(caseSensitive);
            return matcher;
        }

    }

    /**
     * Optimized matcher for trailing wildcards
     */
    private static final class SubpathMatcher implements Matcher {

        private final String subpath;

        private final int length;

        private final boolean caseSensitive;

        private SubpathMatcher(String subpath, boolean caseSensitive) {
            Assert.isTrue(!subpath.contains("*"), "subpath cannot contain \"*\"");
            this.subpath = caseSensitive ? subpath : subpath.toLowerCase();
            this.length = subpath.length();
            this.caseSensitive = caseSensitive;
        }

        @Override
        public boolean matches(String path) {
            if (!this.caseSensitive) {
                path = path.toLowerCase();
            }
            return path.startsWith(this.subpath) && (path.length() == this.length || path.charAt(this.length) == '/');
        }

        @Override
        public Map<String, String> extractUriTemplateVariables(String path) {
            return Collections.emptyMap();
        }
    }
}
