package parking.model;

public final class DeliveryVan extends Car {
    private static final PricingRule PRICING = new DeliveryVanPricing(RATE_PER_HOUR);

    public DeliveryVan(String regNo) {
        super(regNo);
    }

    public DeliveryVan(String regNo, ParkingPass pass) {
        super(regNo, pass);
    }

    @Override
    protected PricingRule pricingRule() {
        return PRICING;
    }
}
