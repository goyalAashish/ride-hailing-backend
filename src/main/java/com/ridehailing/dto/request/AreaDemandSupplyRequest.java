package com.ridehailing.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AreaDemandSupplyRequest(
        @NotNull @Min(0) Integer demand,
        @NotNull @Min(0) Integer supply
) {
}
