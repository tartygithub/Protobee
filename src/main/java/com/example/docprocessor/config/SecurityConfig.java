package com.example.docprocessor.config;

import com.example.docprocessor.service.CustomUserDetailsService;
import com.example.docprocessor.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final AuthConfigProperties authConfigProperties;
    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;

    public SecurityConfig(AuthConfigProperties authConfigProperties,
                          CustomUserDetailsService userDetailsService,
                          @org.springframework.context.annotation.Lazy UserService userService) {
        this.authConfigProperties = authConfigProperties;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable() // disable for ease of dynamic updates & REST Swagger tests
            .authorizeRequests()
                .antMatchers("/login", "/register", "/css/**", "/js/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
                .and()
            .formLogin()
                .loginPage("/login")
                .successHandler(customSuccessHandler())
                .failureHandler(customFailureHandler())
                .permitAll()
                .and()
            .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll();
    }

    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
        List<Object> providers = new ArrayList<>();

        // 1. LDAP Authentication Provider (if enabled)
        if (authConfigProperties.isLdapEnabled()) {
            ActiveDirectoryLdapAuthenticationProvider ldapProvider =
                    new ActiveDirectoryLdapAuthenticationProvider(
                            authConfigProperties.getLdapBase(),
                            authConfigProperties.getLdapUrl()
                    );
            ldapProvider.setConvertSubErrorCodesToExceptions(true);
            providers.add(ldapProvider);
        }

        // 2. Standard DB Authentication Provider (always included as primary or fallback)
        DaoAuthenticationProvider daoProvider = new DaoAuthenticationProvider();
        daoProvider.setUserDetailsService(userDetailsService);
        daoProvider.setPasswordEncoder(passwordEncoder);
        providers.add(daoProvider);

        // Map providers into AuthenticationManager
        return new ProviderManager(providers.stream()
                .map(p -> (org.springframework.security.authentication.AuthenticationProvider) p)
                .collect(Collectors.toList()));
    }

    private AuthenticationSuccessHandler customSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                                Authentication authentication) throws IOException, ServletException {
                String username = authentication.getName();
                userService.resetFailedAttempts(username);
                response.sendRedirect("/");
            }
        };
    }

    private AuthenticationFailureHandler customFailureHandler() {
        return new AuthenticationFailureHandler() {
            @Override
            public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                                AuthenticationException exception) throws IOException, ServletException {
                String username = request.getParameter("username");
                if (username != null) {
                    userService.incrementFailedAttempts(username);
                }
                String errorMessage = "Invalid username or password.";
                if (exception.getMessage().contains("locked")) {
                    errorMessage = "Your account has been locked due to too many failed attempts.";
                } else if (exception.getMessage().contains("expired")) {
                    errorMessage = "Your password has expired. Please contact an administrator to reset it.";
                }
                request.getSession().setAttribute("loginError", errorMessage);
                response.sendRedirect("/login?error=true");
            }
        };
    }
}
