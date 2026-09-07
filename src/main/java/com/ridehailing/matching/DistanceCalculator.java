package com.ridehailing.matching;

import com.ridehailing.domain.Location;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DistanceCalculator {

    public double between(Location first, Location second) {
        Objects.requireNonNull(first, "first location is required");
        Objects.requireNonNull(second, "second location is required");
        return Math.hypot(second.x() - first.x(), second.y() - first.y());
    }
}
