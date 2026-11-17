package parking.model;

import java.time.LocalDateTime;
import java.util.Objects;

public abstract class Vehicle {
    private final String regNo;
    private final ParkingPass pass;

    protected Vehicle(String regNo) {
        this(regNo, null);
    }

    protected Vehicle(String regNo, ParkingPass pass) {
        String normalized = Objects.requireNonNull(regNo, "regNo").trim().toUpperCase();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("registration number cannot be blank");
        }
        this.regNo = normalized;
        this.pass = pass;
    }

    public final String getRegNo() {
        return regNo;
    }

    public final ParkingPass getPass() {
        return pass;
    }

    protected abstract PricingRule pricingRule();

    public final double calculateFee(int minutes) {
        return calculateFee(LocalDateTime.of(2000, 1, 1, 0, 0), minutes);
    }

    public final double calculateFee(LocalDateTime entry, int minutes) {
        Objects.requireNonNull(entry, "entry");
        if (minutes < 0) {
            throw new IllegalArgumentException("minutes cannot be negative");
        }
        return pricingRule().calculate(this, entry, minutes);
    }

    public static int billableHours(int minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("minutes cannot be negative");
        }
        return minutes / 60 + (minutes % 60 == 0 ? 0 : 1);
    }

    @Override
    public final boolean equals(Object other) {
        return other instanceof Vehicle vehicle && regNo.equals(vehicle.regNo);
    }

    @Override
    public final int hashCode() {
        return regNo.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + regNo + "]";
    }
}
