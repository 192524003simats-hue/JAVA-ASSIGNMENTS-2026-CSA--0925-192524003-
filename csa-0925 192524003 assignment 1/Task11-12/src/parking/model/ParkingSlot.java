package parking.model;

public final class ParkingSlot {
    private final int floorNumber;
    private final int slotNumber;
    private final boolean chargingSlot;
    private Vehicle vehicle;

    public ParkingSlot(int floorNumber, int slotNumber, boolean chargingSlot) {
        if (floorNumber < 0 || slotNumber < 0) {
            throw new IllegalArgumentException("slot coordinates cannot be negative");
        }
        this.floorNumber = floorNumber;
        this.slotNumber = slotNumber;
        this.chargingSlot = chargingSlot;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public boolean isChargingSlot() {
        return chargingSlot;
    }

    public boolean isOccupied() {
        return vehicle != null;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void park(Vehicle vehicle) {
        if (this.vehicle != null) {
            throw new IllegalStateException("slot is already occupied");
        }
        this.vehicle = java.util.Objects.requireNonNull(vehicle, "vehicle");
    }

    public void release() {
        if (vehicle == null) {
            throw new IllegalStateException("slot is already free");
        }
        vehicle = null;
    }
}
