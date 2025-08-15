package za.co.sanlam.transferservice.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
    log.info("Spring security disabled, permitAll");
    httpSecurity
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests((auth) -> auth.anyRequest().permitAll());

    return httpSecurity.build();
  }
}
