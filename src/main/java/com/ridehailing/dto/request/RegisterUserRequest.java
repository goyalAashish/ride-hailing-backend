package com.ridehailing.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RegisterUserRequest(
        @NotBlank(message = "must not be blank") String name,
        @NotBlank(message = "must not be blank") String phone
) {
}
