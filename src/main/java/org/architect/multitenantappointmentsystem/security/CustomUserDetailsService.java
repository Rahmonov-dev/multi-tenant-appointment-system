package org.architect.multitenantappointmentsystem.security;

import lombok.RequiredArgsConstructor;
import org.architect.multitenantappointmentsystem.entity.Staff;
import org.architect.multitenantappointmentsystem.entity.User;
import org.architect.multitenantappointmentsystem.entity.UserStatus;
import org.architect.multitenantappointmentsystem.repository.StaffRepository;
import org.architect.multitenantappointmentsystem.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final StaffRepository staffRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User topilmadi: " + email));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UsernameNotFoundException("User faol emas: " + email);
        }

        List<Staff> staffRoles = staffRepository.findByUserId(user.getId());

        if (staffRoles.isEmpty()) {
            Collection<GrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_USER")
            );
            return AuthUser.create(
                    user,
                    List.of(),
                    List.of(),
                    List.of("USER"),
                    authorities
            );
        }

        List<Long> tenantIds = staffRoles.stream()
                .map(staff -> staff.getTenant().getId())
                .distinct()
                .toList();

        List<Long> staffIds = staffRoles.stream()
                .map(Staff::getId)
                .toList();

        List<String> roles = staffRoles.stream()
                .map(staff -> staff.getRole().name())
                .distinct()
                .toList();

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();

        return AuthUser.create(
                user,
                tenantIds,
                staffIds,
                roles,
                authorities
        );
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserByIdForJwt(Long userId, String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User topilmadi: " + userId));

        List<Staff> staffRoles = staffRepository.findByUserId(user.getId());

        if (staffRoles.isEmpty()) {
            Collection<GrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_USER")
            );
            return AuthUser.createWithoutPassword(
                    user.getId(),
                    email,
                    List.of(),
                    List.of(),
                    List.of("USER"),
                    authorities
            );
        }

        List<Long> tenantIds = staffRoles.stream()
                .map(staff -> staff.getTenant().getId())
                .distinct()
                .toList();

        List<Long> staffIds = staffRoles.stream()
                .map(Staff::getId)
                .toList();

        List<String> roles = staffRoles.stream()
                .map(staff -> staff.getRole().name())
                .distinct()
                .toList();

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();

        return AuthUser.createWithoutPassword(
                user.getId(),
                email,
                tenantIds,
                staffIds,
                roles,
                authorities
        );
    }
}
