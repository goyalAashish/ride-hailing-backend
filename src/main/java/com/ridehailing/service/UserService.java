package com.ridehailing.service;

import com.ridehailing.domain.User;
import com.ridehailing.dto.request.RegisterUserRequest;
import com.ridehailing.dto.response.UserResponse;
import com.ridehailing.exception.DuplicateResourceException;
import com.ridehailing.exception.ResourceNotFoundException;
import com.ridehailing.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse register(RegisterUserRequest request) {
        String name = request.name().trim();
        String phone = request.phone().trim();
        if (userRepository.findByPhone(phone).isPresent()) {
            throw new DuplicateResourceException("User", "phone", phone);
        }
        User saved = userRepository.save(new User(name, phone));
        return UserResponse.from(saved);
    }

    public User getRequired(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }
}
