package com.hrms.service;

import com.hrms.dto.LoginRequest;
import com.hrms.entity.User;
import com.hrms.exception.InvalidRequestException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidRequestException("Invalid username or password"));
        // TODO: not recommended
        if (!user.getPassword().equals(request.getPassword())) {
            throw new InvalidRequestException("Invalid username or password");
        }

        return user;
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!user.getPassword().equals(currentPassword)) {
            throw new InvalidRequestException("Current password is incorrect");
        }

        user.setPassword(newPassword);
        userRepository.save(user);
    }
}