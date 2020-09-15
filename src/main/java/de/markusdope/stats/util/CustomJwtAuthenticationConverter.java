package de.markusdope.stats.util;

import de.markusdope.stats.config.MarkusDopeStatsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, Mono<? extends AbstractAuthenticationToken>> {

    private final JwtGrantedAuthoritiesConverter defaultGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Autowired
    private MarkusDopeStatsProperties properties;

    @Override
    public Mono<? extends AbstractAuthenticationToken> convert(Jwt source) {
        return Mono.defer(() -> {
            Collection<GrantedAuthority> authorities = Stream.concat(
                    defaultGrantedAuthoritiesConverter.convert(source).stream(),
                    this.extractResourceRoles(source)
            ).collect(Collectors.toSet());

            return Mono.just(new JwtAuthenticationToken(source, authorities, source.getClaimAsString("preferred_username")));
        });
    }

    private Stream<? extends GrantedAuthority> extractResourceRoles(Jwt source) {
        Map<String, Object> resourceAccess = source.getClaim("resource_access");

        if (resourceAccess != null) {
            Map<String, Object> resource = (Map<String, Object>) resourceAccess.get(properties.getOauthResourceId());
            if (resource != null) {
                Collection<String> resourceRoles = (Collection<String>) resource.get("roles");
                if (resourceRoles != null) {
                    return resourceRoles.stream()
                            .map(x -> new SimpleGrantedAuthority("ROLE_" + x))
                            .collect(Collectors.toSet()).stream();
                }
            }
        }

        return Stream.empty();
    }
}
