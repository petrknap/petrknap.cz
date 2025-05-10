package cz.petrknap.website;

import java.util.List;

public record Config(List<User> users) {
    public record User(String username, String password, List<String> roles) {
    }
}
