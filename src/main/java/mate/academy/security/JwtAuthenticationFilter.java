package mate.academy.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    private final Map<String, List<String>> excludedPathsMap = Map.of(
            "/auth/", List.of("GET", "POST"),
            "/health", List.of("GET"),
            "/cars", List.of("GET")
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (isExcludedPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = getToken(request);

        if (token != null && jwtUtil.isValidToken(token)) {
            String username = jwtUtil.getUsername(token);
            logger.info("Username from token: " + username);

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            logger.info("User details loaded: " + userDetails);
            logger.info("User roles: " + userDetails.getAuthorities());

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private String getToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private boolean isExcludedPath(HttpServletRequest request) {
        String requestPath = request.getServletPath();
        String method = request.getMethod();

        boolean isExcluded = excludedPathsMap.entrySet().stream()
                .anyMatch(entry -> requestPath
                        .startsWith(entry.getKey()) && entry.getValue().contains(method));

        logger.info(String.format(
                "Request path: %s, Method: %s, Is excluded: %b",
                requestPath, method, isExcluded)
        );
        return isExcluded;
    }
}
