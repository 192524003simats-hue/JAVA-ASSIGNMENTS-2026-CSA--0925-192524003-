package parking.model;

import java.time.LocalDateTime;

public final class FlatPricing implements PricingRule {
    private final double feePerVisit;

    public FlatPricing(double feePerVisit) {
        if (feePerVisit < 0) {
            throw new IllegalArgumentException("fee cannot be negative");
        }
        this.feePerVisit = feePerVisit;
    }

    @Override
    public double calculate(Vehicle vehicle, LocalDateTime entry, int minutes) {
        return minutes == 0 ? 0.0 : feePerVisit;
    }
}
