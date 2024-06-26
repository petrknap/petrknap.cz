package cz.petrknap.website;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import cz.petrknap.website.backups.BackupsController;
import cz.petrknap.website.link.LinkToController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    @Value("${website.users-json-file}")
    private String pathToUsersJsonFile;

    private final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(
                requests -> requests
                        .requestMatchers("/error", "/ping").permitAll()
                        .requestMatchers(new AntPathRequestMatcher(BackupsController.MAPPING + "/*/freshness", HttpMethod.GET.name())).permitAll()
                        .requestMatchers(new AntPathRequestMatcher(LinkToController.MAPPING + "/**", HttpMethod.GET.name())).permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        Collection<UserDetails> users = loadUsers().stream().map(user -> org.springframework.security.core.userdetails.User
                .withDefaultPasswordEncoder() // it uses static accounts with onetime passwords
                .username(user.username())
                .password(user.password())
                .roles(user.role().toUpperCase())
                .build()
        ).toList();
        return new InMemoryUserDetailsManager(users);
    }

    private Collection<User> loadUsers() {
        Type usersList = new TypeToken<Collection<User>>(){}.getType();
        String usersJson = "[]";
        try {
            usersJson = Files.readString(Paths.get(pathToUsersJsonFile));
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }
        return new Gson().fromJson(usersJson, usersList);
    }
}
