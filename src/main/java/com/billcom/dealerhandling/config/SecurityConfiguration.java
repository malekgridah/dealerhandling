package com.billcom.dealerhandling.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author malek.gridah
 * @since 1.3
 */

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final BasicAuthFilter authFilter;
    private final ConnectionPoolsAuthenticationManager connectionPoolsAuthenticationManager;

    @Autowired
    public SecurityConfiguration(BasicAuthFilter authFilter,
                                 ConnectionPoolsAuthenticationManager connectionPoolsAuthenticationManager) {
        this.authFilter = authFilter;
        this.connectionPoolsAuthenticationManager = connectionPoolsAuthenticationManager;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeRequests(authorizeRequests ->
                        authorizeRequests
                                .antMatchers("/api/**").authenticated()
                                .anyRequest().permitAll()
                )
                .authenticationManager(connectionPoolsAuthenticationManager)
                .addFilterAfter(authFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic();
        return http.build();
    }
}


