package com.hrms.security;

import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class AccessRules {

    private AccessRules() {
    }

    public static boolean hasAnyRole(Authentication authentication, String... roles) {
        Set<String> wanted = Arrays.stream(roles).map(role -> "ROLE_" + role).collect(Collectors.toSet());
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> wanted.contains(authority.getAuthority()));
    }

    /**
     * ADMIN and HR may query any employee, or everyone when none is given.
     * Everyone else (managers use the separate "pending approvals" endpoint for their team)
     * is always scoped to their own employee id, whatever they ask for.
     */
    public static Long scopeToCaller(Authentication authentication, AuthenticatedUser caller, Long requestedEmployeeId) {
        return hasAnyRole(authentication, "ADMIN", "HR") ? requestedEmployeeId : caller.employeeId();
    }
}
