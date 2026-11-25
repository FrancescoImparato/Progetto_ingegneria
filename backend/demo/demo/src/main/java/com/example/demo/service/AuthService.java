package com.example.demo.service;
// service/AuthService.java
import com.example.demo.dto.*;
import com.example.demo.exception.BusinessException;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new BusinessException("Email già registrata", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByNickname(req.nickname())) {
            throw new BusinessException("Nickname già in uso", HttpStatus.CONFLICT);
        }
        User user = new User(req.email(), passwordEncoder.encode(req.password()), req.nickname(), Role.USER);
        userRepository.save(user);
        return new AuthResponse(jwtService.generateToken(user), UserProfileResponse.from(user));
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
            .orElseThrow(() -> new BusinessException("Credenziali non valide", HttpStatus.UNAUTHORIZED));

        if (!user.isActive()) {
            throw new BusinessException("Account disabilitato", HttpStatus.FORBIDDEN);
        }
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BusinessException("Credenziali non valide", HttpStatus.UNAUTHORIZED);
        }
        return new AuthResponse(jwtService.generateToken(user), UserProfileResponse.from(user));
    }
}
