package cz.petrknap.website;

import jakarta.servlet.ServletRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

// todo: find a way how to implement it as a filter - known problem with filter: missing username with bad credentials

/**
 * Injected value is accessible in Tomcat access log as `%{Username}r`
 */
public class UsernameToRequestInjectingUserDetailsService implements UserDetailsService {
    public static final String REQUEST_ATTRIBUTE = "Username";

    private final ServletRequest request;
    private final UserDetailsService userDetailsService;

    public UsernameToRequestInjectingUserDetailsService(ServletRequest request, UserDetailsService userDetailsService) {
        this.request = request;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        request.setAttribute(REQUEST_ATTRIBUTE, username);
        return userDetailsService.loadUserByUsername(username);
    }
}
