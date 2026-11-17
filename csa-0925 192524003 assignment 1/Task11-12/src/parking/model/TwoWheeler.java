package parking.model;

public class TwoWheeler extends Vehicle {
    private static final PricingRule PRICING = new HourlyPricing(10.0);

    public TwoWheeler(String regNo) {
        super(regNo);
    }

    public TwoWheeler(String regNo, ParkingPass pass) {
        super(regNo, pass);
    }

    @Override
    protected PricingRule pricingRule() {
        return PRICING;
    }
}
