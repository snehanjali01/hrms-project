package com.hrms.security;

/**
 * The logged-in caller, taken from the JWT. Controllers read this instead of trusting
 * ids sent in the request body.
 */
public record AuthenticatedUser(Long userId, Long employeeId, String username) {
}
