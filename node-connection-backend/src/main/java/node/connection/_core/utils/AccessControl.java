package node.connection._core.utils;

import node.connection._core.exception.ExceptionStatus;
import node.connection._core.exception.client.ForbiddenException;
import node.connection._core.security.CustomUserDetails;
import node.connection.entity.constant.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class AccessControl {

    public void hasAdminRole(CustomUserDetails userDetails) {
        boolean granted = userDetails.getAuthorities()
                .stream()
                .anyMatch(authority -> hasRole(authority, Role.ADMIN));

        if (!granted) throw new ForbiddenException(ExceptionStatus.FORBIDDEN);
    }

    public void hasRootRole(CustomUserDetails userDetails) {
        boolean granted = userDetails.getAuthorities()
                .stream()
                .anyMatch(authority -> hasRole(authority, Role.ROOT));

        if (!granted) throw new ForbiddenException(ExceptionStatus.FORBIDDEN);
    }

    public void hasRegistryRole(CustomUserDetails userDetails) {
        boolean granted = userDetails.getAuthorities()
                .stream()
                .anyMatch(authority -> hasRole(authority, Role.REGISTRY));

        if (!granted) throw new ForbiddenException(ExceptionStatus.FORBIDDEN);
    }

    public void hasViewerRole(CustomUserDetails userDetails) {
        boolean granted = userDetails.getAuthorities()
                .stream()
                .anyMatch(authority -> hasRole(authority, Role.VIEWER));

        if (!granted) throw new ForbiddenException(ExceptionStatus.FORBIDDEN);
    }

    public void hasAnonymousRole(CustomUserDetails userDetails) {
        boolean granted = userDetails.getAuthorities()
                .stream()
                .anyMatch(authority -> hasRole(authority, Role.ANONYMOUS));

        if (!granted) throw new ForbiddenException(ExceptionStatus.FORBIDDEN);
    }

    private boolean hasRole(GrantedAuthority authorities, Role role) {
        return authorities.toString().equals(role.getRoleName());
    }
}
