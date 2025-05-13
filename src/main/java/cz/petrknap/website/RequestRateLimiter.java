package cz.petrknap.website;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestRateLimiter extends OncePerRequestFilter {
    private final Semaphore authorizationRequestSemaphore;
    private final Timer timer = new Timer();

    public RequestRateLimiter(Config config) {
        authorizationRequestSemaphore = new Semaphore(config.requestRateLimiter().authorizationRequestsPerMinute());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            if (request.getHeader("Authorization") != null) {
                acquireForMinute(authorizationRequestSemaphore);
            }
            filterChain.doFilter(request, response);
        } catch (LimitExceeded ignored) {
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value());
        }
    }

    private void acquireForMinute(Semaphore semaphore) throws LimitExceeded {
        if (!semaphore.tryAcquire()) {
            throw new LimitExceeded();
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                semaphore.release();
            }
        }, 60000);
    }

    private static class LimitExceeded extends Exception {
    }
}
