package com.ridehailing.dto.request;

import com.ridehailing.domain.enums.CarType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterDriverRequest(
        @NotBlank(message = "must not be blank") String name,
        @NotBlank(message = "must not be blank") String phone,
        @NotBlank(message = "must not be blank") String registrationNumber,
        @NotNull(message = "must not be null") CarType carType
) {
}
