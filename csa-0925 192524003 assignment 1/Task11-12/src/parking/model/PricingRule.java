package parking.model;

import java.time.LocalDateTime;

public interface PricingRule {
    double calculate(Vehicle vehicle, LocalDateTime entry, int minutes);
}
