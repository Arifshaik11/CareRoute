package com.innovation_matrix.careroute.security;

import com.innovation_matrix.careroute.user.AppUserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository users;

    public AppUserDetailsService(AppUserRepository users) {
        this.users = users;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthUser loadUserByUsername(String username) {
        return users.findByUsername(username)
                .map(AuthUser::from)
                .orElseThrow(() -> new UsernameNotFoundException("Unknown user"));
    }
}
