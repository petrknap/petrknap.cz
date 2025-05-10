package cz.petrknap.website;

import java.util.List;

public record Config(ThrottledUserDetailsService throttledUserDetailsService, List<User> users) {
    public record ThrottledUserDetailsService(int throttleAfterPermits, int releasePermitAfterMs, int lockWhenDelayReachesMs) {}
    public record User(String username, String password, List<String> roles) {
    }
}
