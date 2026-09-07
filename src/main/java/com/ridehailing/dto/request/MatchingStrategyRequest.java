package com.ridehailing.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MatchingStrategyRequest(@NotBlank String strategy) {
}
