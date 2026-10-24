package com.innovation_matrix.careroute.auth;

import com.innovation_matrix.careroute.audit.AuditService;
import com.innovation_matrix.careroute.auth.AuthDtos.LoginRequest;
import com.innovation_matrix.careroute.auth.AuthDtos.LoginResponse;
import com.innovation_matrix.careroute.auth.AuthDtos.UserDto;
import com.innovation_matrix.careroute.common.error.NotFoundException;
import com.innovation_matrix.careroute.security.AuthUser;
import com.innovation_matrix.careroute.security.JwtService;
import com.innovation_matrix.careroute.user.AppUserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AppUserRepository users;
    private final AuditService audit;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService,
                       AppUserRepository users, AuditService audit) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.users = users;
        this.audit = audit;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username().trim(), request.password()));
        AuthUser principal = (AuthUser) auth.getPrincipal();
        JwtService.IssuedToken token = jwtService.issue(principal);
        audit.record(principal.getId(), "LOGIN", "USER", principal.getId(), null);
        return new LoginResponse(token.token(), token.expiresAt(), currentUser(principal.getId()));
    }

    @Transactional(readOnly = true)
    public UserDto currentUser(Long userId) {
        return users.findById(userId)
                .map(UserDto::from)
                .orElseThrow(() -> new NotFoundException("User", userId));
    }
}
