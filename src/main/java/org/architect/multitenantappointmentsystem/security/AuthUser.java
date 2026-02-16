package org.architect.multitenantappointmentsystem.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jdk.dynalink.linker.LinkerServices;
import lombok.Getter;
import org.architect.multitenantappointmentsystem.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.Collection;
import java.util.List;
@Getter
public class AuthUser implements UserDetails {

    private final Long userId;
    private final String email;
    @JsonIgnore
    private final String passwordHash;

    private final List<Long> tenantIds; // Bir nechta tenant bo‘lsa
    private final List<Long> staffIds;  // Bir nechta staff id
    private final List<String> roles;   // Bir nechta role

    private final Collection<? extends GrantedAuthority> authorities;

    private AuthUser(
            Long userId,
            String email,
            String passwordHash,
            List<Long> tenantIds,
            List<Long> staffIds,
            List<String> roles,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.tenantIds = tenantIds;
        this.staffIds = staffIds;
        this.roles = roles;
        this.authorities = authorities;
    }

    public static AuthUser create(
            User user,
            List<Long> tenantIds,
            List<Long> staffIds,
            List<String> roles,
            Collection<? extends GrantedAuthority> authorities
    ) {
        return new AuthUser(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                tenantIds,
                staffIds,
                roles,
                authorities
        );
    }

    public static AuthUser createWithoutPassword(
            Long userId,
            String email,
            List<Long> tenantIds,
            List<Long> staffIds,
            List<String> roles,
            Collection<? extends GrantedAuthority> authorities
    ) {
        return new AuthUser(
                userId,
                email,
                "",
                tenantIds,
                staffIds,
                roles,
                authorities
        );
    }

    @Override
    public String getUsername() { return email; }

    @Override
    public String getPassword() { return passwordHash; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

}
