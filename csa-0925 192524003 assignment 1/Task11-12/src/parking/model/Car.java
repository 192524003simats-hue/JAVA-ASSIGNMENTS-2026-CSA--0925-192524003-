package parking.model;

public class Car extends Vehicle {
    protected static final double RATE_PER_HOUR = 30.0;
    private static final PricingRule PRICING = new HourlyPricing(RATE_PER_HOUR);

    public Car(String regNo) {
        this(regNo, null);
    }

    public Car(String regNo, ParkingPass pass) {
        super(regNo, pass);
    }

    @Override
    protected PricingRule pricingRule() {
        return PRICING;
    }
}
