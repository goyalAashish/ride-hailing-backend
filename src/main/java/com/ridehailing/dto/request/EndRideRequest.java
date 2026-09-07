package com.ridehailing.dto.request;

import jakarta.validation.constraints.NotNull;

public record EndRideRequest(
        @NotNull(message = "must not be null") Double destX,
        @NotNull(message = "must not be null") Double destY
) {
}
