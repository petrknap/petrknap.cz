package cz.petrknap.website;

import java.util.List;

public record Config(BootGuard bootGuard, RequestRateLimiter requestRateLimiter, List<User> users) {
    public record BootGuard(String sqliteCopyFile) {
    }
    public record RequestRateLimiter(int authorizationRequestsPerMinute) {
    }
    public record User(String username, String password, List<String> roles) {
    }
}
