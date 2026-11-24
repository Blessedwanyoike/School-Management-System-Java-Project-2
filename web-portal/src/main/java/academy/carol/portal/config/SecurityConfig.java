package academy.carol.portal.config;

import academy.carol.portal.repository.UserAccountRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.context.ApplicationEventPublisher;

@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationEventPublisher authenticationEventPublisher(ApplicationEventPublisher publisher) {
        return new DefaultAuthenticationEventPublisher(publisher);
    }

    @Bean
    UserDetailsService userDetailsService(UserAccountRepository users) {
        return username -> users.findByUsername(username)
                .map(account -> User.withUsername(account.getUsername())
                        .password(account.getPasswordHash())
                        .roles(account.getRole().name())
                        .disabled(!account.isEnabled())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Account not found"));
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/login", "/signup", "/css/**", "/error").permitAll()
                        .requestMatchers("/admin", "/admin/**").hasAnyRole("HEAD_TEACHER", "DEPUTY_HEAD_TEACHER", "SECRETARY")
                        .requestMatchers("/bursar", "/bursar/**").hasRole("BURSAR")
                        .requestMatchers("/head-teacher/**").hasAnyRole("HEAD_TEACHER", "DEPUTY_HEAD_TEACHER", "SECRETARY")
                        .requestMatchers("/modules", "/modules/**", "/reports", "/reports/**")
                            .hasAnyRole("HEAD_TEACHER", "DEPUTY_HEAD_TEACHER", "SECRETARY", "BURSAR")
                        .requestMatchers("/logs").hasAnyRole("HEAD_TEACHER", "DEPUTY_HEAD_TEACHER", "SECRETARY", "BURSAR")
                        .requestMatchers("/teacher", "/teacher/**").hasRole("TEACHER")
                        .requestMatchers("/driver", "/driver/**").hasRole("BUS_DRIVER")
                        .requestMatchers("/parent", "/parent/**").hasRole("PARENT")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/portal", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .build();
    }
}
