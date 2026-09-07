package com.ridehailing.dto.response;

import com.ridehailing.domain.User;

public record UserResponse(Long userId, String name, String phone) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getUserId(), user.getName(), user.getPhone());
    }
}
