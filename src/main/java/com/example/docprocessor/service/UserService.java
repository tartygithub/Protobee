package com.example.docprocessor.service;

import com.example.docprocessor.model.User;
import com.example.docprocessor.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public User registerUser(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        user.setLocked(false);
        // Expiration default: 90 days from now
        user.setPasswordExpirationDate(LocalDateTime.now().plusDays(90));
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public void lockUser(Long id) {
        userRepository.findById(id).ifPresent(u -> {
            u.setLocked(true);
            userRepository.save(u);
        });
    }

    @Transactional
    public void unlockUser(Long id) {
        userRepository.findById(id).ifPresent(u -> {
            u.setLocked(false);
            u.setFailedLoginAttempts(0);
            userRepository.save(u);
        });
    }

    @Transactional
    public void resetPassword(Long id, String newPassword) {
        userRepository.findById(id).ifPresent(u -> {
            u.setPassword(passwordEncoder.encode(newPassword));
            u.setPasswordExpirationDate(LocalDateTime.now().plusDays(90));
            userRepository.save(u);
        });
    }

    @Transactional
    public void setPasswordExpired(Long id) {
        userRepository.findById(id).ifPresent(u -> {
            // Set password expiration to 1 day ago to trigger expiration screen/checks
            u.setPasswordExpirationDate(LocalDateTime.now().minusDays(1));
            userRepository.save(u);
        });
    }

    @Transactional
    public void incrementFailedAttempts(String username) {
        userRepository.findByUsername(username).ifPresent(u -> {
            int attempts = u.getFailedLoginAttempts() + 1;
            u.setFailedLoginAttempts(attempts);
            if (attempts >= 5) {
                u.setLocked(true);
            }
            userRepository.save(u);
        });
    }

    @Transactional
    public void resetFailedAttempts(String username) {
        userRepository.findByUsername(username).ifPresent(u -> {
            u.setFailedLoginAttempts(0);
            userRepository.save(u);
        });
    }
}
