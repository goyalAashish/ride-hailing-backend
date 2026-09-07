package com.ridehailing.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateLocationRequest(
        @NotNull(message = "must not be null") Double x,
        @NotNull(message = "must not be null") Double y
) {
}
