package parking.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public final class ParkingTicket {
    private static final AtomicLong NEXT_NUMBER = new AtomicLong(1);

    private final String ticketNumber;
    private final Vehicle vehicle;
    private final ParkingSlot slot;
    private final LocalDateTime entryTime;

    public ParkingTicket(Vehicle vehicle, ParkingSlot slot, LocalDateTime entryTime) {
        this.ticketNumber = "PK-" + NEXT_NUMBER.getAndIncrement();
        this.vehicle = Objects.requireNonNull(vehicle, "vehicle");
        this.slot = Objects.requireNonNull(slot, "slot");
        this.entryTime = Objects.requireNonNull(entryTime, "entryTime");
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public ParkingSlot getSlot() {
        return slot;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }
}
