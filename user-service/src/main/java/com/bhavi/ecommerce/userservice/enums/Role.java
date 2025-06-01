package com.bhavi.ecommerce.userservice.enums;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ROLE_CUSTOMER,
    ROLE_ADMIN,
    ROLE_SELLER;

    // Spring Security requires roles to be returned as strings (e.g., "ADMIN")
    @Override
    public String getAuthority() {
        return name();
    }
}
