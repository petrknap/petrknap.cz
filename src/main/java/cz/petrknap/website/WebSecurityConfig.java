package cz.petrknap.website;

import cz.petrknap.website.backups.BackupsController;
import cz.petrknap.website.link.LinkToController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    private final Config config;

    public WebSecurityConfig(Config config) {
        this.config = config;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(
                requests -> requests
                        .requestMatchers("/error", "/ping").permitAll()
                        .requestMatchers("/404/**").permitAll() // fake route to render 404 for reverse proxy
                        .requestMatchers(new AntPathRequestMatcher(BackupsController.MAPPING + "/*/freshness", HttpMethod.GET.name())).permitAll()
                        .requestMatchers(new AntPathRequestMatcher(BackupsController.MAPPING + "/*/freshness", HttpMethod.PUT.name())).hasRole("USER")
                        .requestMatchers(new AntPathRequestMatcher(LinkToController.MAPPING + "/**", HttpMethod.GET.name())).permitAll()
                        .anyRequest().hasRole("ADMIN")
                )
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new ThrottledUserDetailsService(new InMemoryUserDetailsManager(config.users().stream().map(user -> org.springframework.security.core.userdetails.User
                .withDefaultPasswordEncoder() // it uses static accounts with onetime passwords
                .username(user.username())
                .password(user.password())
                .roles(user.roles().stream().map(String::toUpperCase).toArray(String[]::new))
                .build()
        ).toArray(UserDetails[]::new)));
    }
}
