package com.example.demo.controller;

// controller/UserController.java
import com.example.demo.dto.UpdateProfileRequest;
import com.example.demo.dto.UserProfileResponse;
import com.example.demo.exception.BusinessException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public UserProfileResponse getMyProfile(Authentication auth) {
        User user = findByAuth(auth);
        return UserProfileResponse.from(user);
    }

    @PutMapping("/me")
    public UserProfileResponse updateMyProfile(Authentication auth, @Valid @RequestBody UpdateProfileRequest req) {
        User user = findByAuth(auth);
        if (!user.getNickname().equals(req.nickname()) && userRepository.existsByNickname(req.nickname())) {
            throw new BusinessException("Nickname già in uso", HttpStatus.CONFLICT);
        }
        user.updateProfile(req.nickname(), req.bio());
        userRepository.save(user);
        return UserProfileResponse.from(user);
    }

    private User findByAuth(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new BusinessException("Utente non trovato", HttpStatus.NOT_FOUND));
    }
}