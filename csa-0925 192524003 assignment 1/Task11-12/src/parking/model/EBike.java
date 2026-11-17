package parking.model;

public final class EBike extends Vehicle {
    private static final PricingRule PRICING = new FlatPricing(25.0);

    public EBike(String regNo) {
        super(regNo);
    }

    public EBike(String regNo, ParkingPass pass) {
        super(regNo, pass);
    }

    @Override
    protected PricingRule pricingRule() {
        return PRICING;
    }
}
