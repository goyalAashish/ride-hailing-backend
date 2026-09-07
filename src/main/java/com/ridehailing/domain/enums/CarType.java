package com.ridehailing.domain.enums;

/**
 * Supported vehicle categories. {@link #SUV} is included so the matching and pricing
 * engines can grow without a schema change; Hatchback and Sedan are the baseline types
 * required by the exercise.
 */
public enum CarType {
    HATCHBACK,
    SEDAN,
    SUV
}
