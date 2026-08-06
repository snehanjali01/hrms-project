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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest{

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp(){
        user = new User();
        user.setId(1L);
        user.setUsername("neha.singh");
        user.setPassword("SecurePass123");
    }

    @Test
    void login_shouldReturnUser_whenCredentialsAreCorrect(){
        LoginRequest request = new LoginRequest();
        request.setUsername("neha.singh");
        request.setPassword("SecurePass123");

        when(userRepository.findByUsername("neha.singh")).thenReturn(Optional.of(user));

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

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Invalid username or password");
    }

    @Test
    void changePassword_shouldUpdatePassword_whenCurrentPasswordIsCorrect() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.changePassword(1L, "SecurePass123", "NewPass456");
        assertThat(user.getPassword()).isEqualTo("NewPass456");
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

        assertThatThrownBy(() -> userService.changePassword(1L, "WrongOldPassword", "NewPass456"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("incorrect");
        verify(userRepository, never()).save(any());
    }
}