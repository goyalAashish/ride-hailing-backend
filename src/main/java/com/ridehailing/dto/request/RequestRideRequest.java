package com.ridehailing.dto.request;

import com.ridehailing.domain.enums.CarType;
import jakarta.validation.constraints.NotNull;

public record RequestRideRequest(
        @NotNull(message = "must not be null") Double pickupX,
        @NotNull(message = "must not be null") Double pickupY,
        @NotNull(message = "must not be null") Double destX,
        @NotNull(message = "must not be null") Double destY,
        @NotNull(message = "must not be null") CarType carType,
        String couponCode
) {
}
