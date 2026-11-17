package parking.model;

public final class ParkingReceipt {
    private final ParkingTicket ticket;
    private final long parkedMinutes;
    private final double parkingFee;
    private final double chargingFee;

    public ParkingReceipt(ParkingTicket ticket, long parkedMinutes,
                          double parkingFee, double chargingFee) {
        this.ticket = java.util.Objects.requireNonNull(ticket, "ticket");
        this.parkedMinutes = parkedMinutes;
        this.parkingFee = parkingFee;
        this.chargingFee = chargingFee;
    }

    public ParkingTicket getTicket() {
        return ticket;
    }

    public long getParkedMinutes() {
        return parkedMinutes;
    }

    public double getParkingFee() {
        return parkingFee;
    }

    public double getChargingFee() {
        return chargingFee;
    }

    public double getTotalFee() {
        return parkingFee + chargingFee;
    }
}
