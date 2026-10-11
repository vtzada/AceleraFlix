package vitortheof.com.br.aceleraflix.shared.infrastructure;

import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import static vitortheof.com.br.aceleraflix.shared.infrastructure.RateLimitingService.Policy;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final RateLimitingService rateLimiter;
    private final boolean trustForwardedFor;

    public RateLimitFilter(RateLimitingService rateLimiter,
                           @Value("${rate-limit.trust-forwarded-for:false}") boolean trustForwardedFor) {
        this.rateLimiter = rateLimiter;
        this.trustForwardedFor = trustForwardedFor;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equals(request.getMethod()) ||
                ("GET".equals(request.getMethod()) && "/actuator/health".equals(request.getServletPath()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String ip = request.getRemoteAddr();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (trustForwardedFor && forwarded != null && !forwarded.isBlank()) {
            ip = forwarded.split(",", 2)[0].trim();
        }
        Policy policy = policy(request);
        ConsumptionProbe probe = rateLimiter.resolverBucket(ip, policy).tryConsumeAndReturnRemaining(1);
        response.setHeader("X-RateLimit-Limit", String.valueOf(rateLimiter.limit(policy)));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
            return;
        }
        long seconds = Math.max(1, (probe.getNanosToWaitForRefill() + TimeUnit.SECONDS.toNanos(1) - 1)
                / TimeUnit.SECONDS.toNanos(1));
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(seconds));
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"mensagem\":\"Muitas requisições. Tente novamente em " + seconds +
                " segundos.\",\"status\":429,\"retryAfterSeconds\":" + seconds + "}");
    }

    private Policy policy(HttpServletRequest request) {
        String path = request.getServletPath();
        if ("POST".equals(request.getMethod())) {
            if ("/auth/login".equals(path) || "/auth/register".equals(path)) return Policy.AUTH;
            if ("/auth/refresh".equals(path)) return Policy.REFRESH;
        }
        return "GET".equals(request.getMethod()) || "HEAD".equals(request.getMethod()) ? Policy.READ : Policy.WRITE;
    }
}
