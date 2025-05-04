package cz.petrknap.website;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

public class ThrottledUserDetailsService implements UserDetailsService {
    private static final int THROTTLE_AFTER_PERMITS = 3;
    private static final int RELEASE_PERMIT_AFTER_MS = 1000 * THROTTLE_AFTER_PERMITS;
    private static final int LOCK_WHEN_DELAY_REACHES_MS = 60000;
    private static final int LOCK_AFTER_PERMITS = LOCK_WHEN_DELAY_REACHES_MS / (RELEASE_PERMIT_AFTER_MS / THROTTLE_AFTER_PERMITS);

    private final UserDetailsService userDetailsService;
    private final Semaphore throttlingSemaphore = new Semaphore(THROTTLE_AFTER_PERMITS);
    private final Semaphore lockingSemaphore = new Semaphore(LOCK_AFTER_PERMITS);
    private final Timer timer = new Timer();

    public ThrottledUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            if (!lockingSemaphore.tryAcquire()) {
                return User.withUsername(username).accountLocked(true).build();
            }
            throttlingSemaphore.acquire();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    lockingSemaphore.release();
                    throttlingSemaphore.release();
                }
            }, RELEASE_PERMIT_AFTER_MS);
            return userDetailsService.loadUserByUsername(username);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UsernameNotFoundException("Caught interruption exception", e);
        }
    }
}
