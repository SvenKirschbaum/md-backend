package de.markusdope.stats.util;

import org.springframework.http.CacheControl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@Component
public class StaticDataCacheFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (exchange.getRequest().getPath().pathWithinApplication().value().startsWith("/static")) {
            exchange.getResponse().getHeaders().setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());
        }
        return chain.filter(exchange);
    }
}
