package hu.klavorar.kubernetesorchestrator.config;

import hu.klavorar.kubernetesorchestrator.filter.JWTAuthenticationFilter;
import hu.klavorar.kubernetesorchestrator.filter.JWTAuthorizationFilter;
import hu.klavorar.kubernetesorchestrator.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtConfiguration jwtConfig;

    @Autowired
    private CorsConfig corsConfiguration;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable().authorizeRequests()
                .antMatchers(HttpMethod.POST, "/login").permitAll()// allow login URL
                .antMatchers(HttpMethod.GET, "/actuator/**").permitAll()// allow actuator paths
                .antMatchers("/v2/api-docs**").permitAll()// allow swagger-ui paths
                .antMatchers("/swagger-ui.html**").permitAll()// allow swagger-ui paths
                .antMatchers("/swagger-resources**").permitAll()// allow swagger-ui paths
                .antMatchers("/swagger-resources/**").permitAll()// allow swagger-ui paths
                .antMatchers("/webjars/**").permitAll()// allow webjars paths
                .anyRequest().authenticated()
                .and()
                .addFilter(new JWTAuthenticationFilter(authenticationManager(), jwtConfig))
                .addFilter(new JWTAuthorizationFilter(authenticationManager(), jwtConfig))
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder());
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration().applyPermitDefaultValues();
        corsConfig.addAllowedHeader("Authorization");
        corsConfig.addExposedHeader("Authorization");
        corsConfig.setAllowedOrigins(corsConfiguration.getAllowedOrigins());
        corsConfig.setAllowCredentials(Boolean.TRUE);
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }

}
