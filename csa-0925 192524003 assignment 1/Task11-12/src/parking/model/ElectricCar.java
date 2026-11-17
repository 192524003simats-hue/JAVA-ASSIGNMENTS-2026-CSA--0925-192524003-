package parking.model;

public final class ElectricCar extends Car implements Chargeable {
    public ElectricCar(String regNo) {
        super(regNo);
    }

    public ElectricCar(String regNo, ParkingPass pass) {
        super(regNo, pass);
    }
}
