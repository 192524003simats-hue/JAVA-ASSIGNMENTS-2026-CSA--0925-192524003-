package parking.model;

import java.util.Locale;

public final class VehicleFactory {
    private VehicleFactory() {
    }

    public static Vehicle create(String regNo, String type, ParkingPass pass) {
        if (type == null) {
            throw new IllegalArgumentException("vehicle type cannot be null");
        }
        return switch (type.trim().toLowerCase(Locale.ROOT)) {
            case "two-wheeler" -> new TwoWheeler(regNo, pass);
            case "car" -> new Car(regNo, pass);
            case "electric-car" -> new ElectricCar(regNo, pass);
            case "e-bike" -> new EBike(regNo, pass);
            case "delivery-van" -> new DeliveryVan(regNo, pass);
            default -> throw new IllegalArgumentException("unknown vehicle type: " + type);
        };
    }
}
