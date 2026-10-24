package com.innovation_matrix.careroute.auth;

import com.innovation_matrix.careroute.auth.AuthDtos.LoginRequest;
import com.innovation_matrix.careroute.auth.AuthDtos.LoginResponse;
import com.innovation_matrix.careroute.auth.AuthDtos.UserDto;
import com.innovation_matrix.careroute.security.AuthUser;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserDto me(@AuthenticationPrincipal AuthUser user) {
        return authService.currentUser(user.getId());
    }
}
