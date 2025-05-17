package cz.petrknap.website;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestRateLimiter extends OncePerRequestFilter {
    private static final Integer ONE_MINUTE_IN_SECONDS = 60;
    private static final Integer ONE_MINUTE_IN_MILLISECONDS = ONE_MINUTE_IN_SECONDS * 1000;

    private final AtomicInteger authorizationRequestsPerMinute = new AtomicInteger(0);
    private final Config.RequestRateLimiter config;

    public RequestRateLimiter(Config config) {
        this.config = config.requestRateLimiter();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                authorizationRequestsPerMinute.set(0);
            }
        }, ONE_MINUTE_IN_MILLISECONDS, ONE_MINUTE_IN_MILLISECONDS);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            if (request.getHeader("Authorization") != null) {
                acquire(authorizationRequestsPerMinute, config.authorizationRequestsPerMinute());
            }
            filterChain.doFilter(request, response);
        } catch (LimitExceeded ignored) {
            response.addHeader(HttpHeaders.RETRY_AFTER, ONE_MINUTE_IN_SECONDS.toString());
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value());
        }
    }

    private static void acquire(AtomicInteger counter, Integer limit) throws LimitExceeded {
        if (counter.updateAndGet(count -> count == Integer.MAX_VALUE ? count : count + 1) > limit) {
            throw new LimitExceeded();
        }
    }

    private static class LimitExceeded extends Exception {
    }
}
