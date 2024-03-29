package de.markusdope.stats.config;

import com.github.cloudyrock.spring.v5.EnableMongock;
import de.markusdope.stats.util.CustomJwtAuthenticationConverter;
import de.markusdope.stats.util.JodaDateTimeConverter;
import de.markusdope.stats.util.JodaDurationConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@EnableMongock
public class MarkusDopeStatsConfig {

    @Autowired
    CustomJwtAuthenticationConverter jwtAuthenticationConverter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf().disable()
                .cors().and()
                .oauth2ResourceServer().jwt().jwtAuthenticationConverter(jwtAuthenticationConverter).and().and()
                .headers().disable()
                .authorizeExchange()
                .matchers(EndpointRequest.toAnyEndpoint())
                .hasRole("actuator")
                .anyExchange().permitAll()
                .and()
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedHeaders(Arrays.asList("origin", "content-type", "accept", "authorization"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    @Autowired
    private JodaDurationConverter jodaDurationConverter;

    @Autowired
    private JodaDateTimeConverter jodaDateTimeConverter;

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        List<Converter> list = new ArrayList<>();
        list.add(jodaDurationConverter);
        list.add(jodaDateTimeConverter);
        return new MongoCustomConversions(list);
    }
}
