package parking.model;

import java.time.LocalDateTime;

public final class HourlyPricing implements PricingRule {
    private final double ratePerStartedHour;

    public HourlyPricing(double ratePerStartedHour) {
        if (ratePerStartedHour < 0) {
            throw new IllegalArgumentException("rate cannot be negative");
        }
        this.ratePerStartedHour = ratePerStartedHour;
    }

    @Override
    public double calculate(Vehicle vehicle, LocalDateTime entry, int minutes) {
        return Vehicle.billableHours(minutes) * ratePerStartedHour;
    }
}
