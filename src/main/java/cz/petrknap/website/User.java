package cz.petrknap.website;

import java.util.List;

public record User(String username, String password, List<String> roles) {
}
