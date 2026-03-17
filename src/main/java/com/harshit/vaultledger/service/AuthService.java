package com.harshit.vaultledger.service;

import com.harshit.vaultledger.dto.AuthRequest;
import com.harshit.vaultledger.dto.AuthResponse;
import com.harshit.vaultledger.dto.RegisterRequest;
import com.harshit.vaultledger.exception.DuplicateUsernameException;
import com.harshit.vaultledger.model.Role;
import com.harshit.vaultledger.model.User;
import com.harshit.vaultledger.repository.UserRepository;
import com.harshit.vaultledger.config.JwtProperties;
import com.harshit.vaultledger.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateUsernameException(request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateUsernameException(request.email());
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(user);
    }

    public AuthResponse authenticate(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(), request.password()));

        String token = jwtTokenProvider.generateToken(authentication);

        return new AuthResponse(token, "Bearer", jwtProperties.expirationMs() / 1000);
    }
}
