package cz.petrknap.website;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

public class ThrottledUserDetailsService implements UserDetailsService {
    private final UserDetailsService userDetailsService;
    private final Config.ThrottledUserDetailsService config;
    private final Semaphore throttlingSemaphore;
    private final Semaphore lockingSemaphore;
    private final Timer timer = new Timer();

    public ThrottledUserDetailsService(UserDetailsService userDetailsService, Config config) {
        this.userDetailsService = userDetailsService;
        this.config = config.throttledUserDetailsService();
        throttlingSemaphore = new Semaphore(this.config.throttleAfterPermits());
        lockingSemaphore = new Semaphore(this.config.lockWhenDelayReachesMs() / this.config.releasePermitAfterMs());
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
            }, (long) this.config.releasePermitAfterMs() * this.config.throttleAfterPermits());
            return userDetailsService.loadUserByUsername(username);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UsernameNotFoundException("Caught interruption exception", e);
        }
    }
}
