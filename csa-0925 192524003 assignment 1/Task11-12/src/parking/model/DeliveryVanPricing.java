package parking.model;

import java.time.LocalDateTime;
import java.time.LocalTime;

public final class DeliveryVanPricing implements PricingRule {
    private static final LocalTime DOUBLE_RATE_START = LocalTime.of(9, 0);
    private static final LocalTime DOUBLE_RATE_END = LocalTime.of(17, 0);
    private final double carRatePerStartedHour;

    public DeliveryVanPricing(double carRatePerStartedHour) {
        if (carRatePerStartedHour < 0) {
            throw new IllegalArgumentException("rate cannot be negative");
        }
        this.carRatePerStartedHour = carRatePerStartedHour;
    }

    @Override
    public double calculate(Vehicle vehicle, LocalDateTime entry, int minutes) {
        int hours = Vehicle.billableHours(minutes);
        double fee = 0.0;
        for (int hour = 0; hour < hours; hour++) {
            LocalTime blockStart = entry.plusHours(hour).toLocalTime();
            boolean doubleRate = !blockStart.isBefore(DOUBLE_RATE_START)
                    && blockStart.isBefore(DOUBLE_RATE_END);
            fee += carRatePerStartedHour * (doubleRate ? 2 : 1);
        }
        return fee;
    }
}
