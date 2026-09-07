package com.ridehailing.domain;

/**
 * Cartesian coordinate value object used for pickup, destination, and driver presence.
 * Distance calculations live with the matching strategy (Step 5), not on this type,
 * so Location stays a pure value.
 */
public record Location(double x, double y) {
}
