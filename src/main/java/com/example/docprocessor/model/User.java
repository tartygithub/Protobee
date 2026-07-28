package com.example.docprocessor.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private boolean enabled = true;
    private boolean locked = false;

    private LocalDateTime passwordExpirationDate;
    private int failedLoginAttempts = 0;

    public User() {}

    public User(String username, String password, LocalDateTime passwordExpirationDate) {
        this.username = username;
        this.password = password;
        this.passwordExpirationDate = passwordExpirationDate;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public LocalDateTime getPasswordExpirationDate() { return passwordExpirationDate; }
    public void setPasswordExpirationDate(LocalDateTime passwordExpirationDate) { this.passwordExpirationDate = passwordExpirationDate; }

    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }

    public boolean isPasswordExpired() {
        if (passwordExpirationDate == null) return false;
        return LocalDateTime.now().isAfter(passwordExpirationDate);
    }
}
