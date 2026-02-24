package com.digibnk.transaction.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Propagates the incoming JWT to all outgoing Feign requests.
 */
@Slf4j
@Component
public class FeignAuthInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String token = jwtAuth.getToken().getTokenValue();
            template.header("Authorization", "Bearer " + token);
            log.debug("Forwarding JWT to Feign request: {}", template.url());
        } else {
            log.debug("No JWT found in SecurityContext for Feign request: {}", template.url());
        }
    }
}
