package top.zenyoung.boot.matcher;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

/**
 * 请求匹配
 *
 * @author young
 */
public interface RequestMatcher {

    /**
     * Decides whether the rule implemented by the strategy matches the supplied request.
     *
     * @param request the request to check for a match
     * @return true if the request matches, false otherwise
     */
    boolean matches(@Nonnull final HttpServletRequest request);

    /**
     * Returns a MatchResult for this RequestMatcher The default implementation returns
     * {@link Collections#emptyMap()} when {@link MatchResult#getVariables()} is invoked.
     *
     * @return the MatchResult from comparing this RequestMatcher against the
     * HttpServletRequest
     * @since 5.2
     */
    default MatchResult matcher(@Nonnull final HttpServletRequest request) {
        boolean match = matches(request);
        return new MatchResult(match, Collections.emptyMap());
    }

    /**
     * The result of matching against an HttpServletRequest Contains the status, true or
     * false, of the match and if present, any variables extracted from the match
     *
     * @since 5.2
     */
    @Getter
    @RequiredArgsConstructor
    class MatchResult {
        private final boolean match;
        private final Map<String, String> variables;

        /**
         * Creates an instance of {@link MatchResult} that is a match with no variables
         *
         * @return MatchResult
         */
        public static MatchResult match() {
            return new MatchResult(true, Collections.emptyMap());
        }

        /**
         * Creates an instance of {@link MatchResult} that is a match with the specified
         * variables
         *
         * @param variables variables
         * @return MatchResult
         */
        public static MatchResult match(final Map<String, String> variables) {
            return new MatchResult(true, variables);
        }

        /**
         * Creates an instance of {@link MatchResult} that is not a match.
         *
         * @return MatchResult
         */
        public static MatchResult notMatch() {
            return new MatchResult(false, Collections.emptyMap());
        }
    }
}
