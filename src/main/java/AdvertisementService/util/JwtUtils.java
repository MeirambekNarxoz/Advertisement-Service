package AdvertisementService.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class JwtUtils {
    
    public UUID extractUserId(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userId = jwt.getClaim("sub"); // или "preferred_username"
        return UUID.fromString(userId);
    }
    
    public boolean hasRole(Authentication authentication, String role) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        // Проверка ролей из claims
        return jwt.getClaimAsStringList("realm_access").contains(role);
    }
}
