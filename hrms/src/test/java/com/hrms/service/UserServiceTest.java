package com.hrms.service;

import com.hrms.dto.LoginRequest;
import com.hrms.entity.User;
import com.hrms.exception.InvalidRequestException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest{

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private static final String STORED_HASH = "$2a$10$storedHashForSecurePass123";

    private User user;

    @BeforeEach
    void setUp(){
        user = new User();
        user.setId(1L);
        user.setUsername("neha.singh");
        user.setPassword(STORED_HASH);
    }

    @Test
    void login_shouldReturnUser_whenCredentialsAreCorrect(){
        LoginRequest request = new LoginRequest();
        request.setUsername("neha.singh");
        request.setPassword("SecurePass123");

        when(userRepository.findByUsername("neha.singh")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("SecurePass123", STORED_HASH)).thenReturn(true);

        User result = userService.login(request);

        assertThat(result.getUsername()).isEqualTo("neha.singh");
    }

    @Test
    void login_shouldThrow_whenUsernameNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUsername("ghost");
        request.setPassword("whatever");

        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Invalid username or password");
    }

    @Test
    void login_shouldThrow_whenPasswordIsIncorrect() {
        LoginRequest request = new LoginRequest();
        request.setUsername("neha.singh");
        request.setPassword("WrongPassword");

        when(userRepository.findByUsername("neha.singh")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", STORED_HASH)).thenReturn(false);

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Invalid username or password");
    }

    @Test
    void changePassword_shouldUpdatePassword_whenCurrentPasswordIsCorrect() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("SecurePass123", STORED_HASH)).thenReturn(true);
        when(passwordEncoder.encode("NewPass456")).thenReturn("$2a$10$newHash");

        userService.changePassword(1L, "SecurePass123", "NewPass456");

        assertThat(user.getPassword()).isEqualTo("$2a$10$newHash");
        assertThat(user.getPassword()).isNotEqualTo("NewPass456");
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changePassword(99L, "old", "new"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_shouldThrow_whenCurrentPasswordIsWrong() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongOldPassword", STORED_HASH)).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(1L, "WrongOldPassword", "NewPass456"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("incorrect");
        verify(userRepository, never()).save(any());
    }
}