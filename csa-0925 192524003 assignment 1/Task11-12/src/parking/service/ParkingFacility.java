package parking.service;

import parking.model.Chargeable;
import parking.model.ParkingPass;
import parking.model.ParkingReceipt;
import parking.model.ParkingSlot;
import parking.model.ParkingTicket;
import parking.model.Vehicle;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ParkingFacility {
    private final ParkingSlot[][] floors;
    private final Map<String, ParkingTicket> activeTickets = new HashMap<>();

    public ParkingFacility(int[] slotsPerFloor) {
        this(slotsPerFloor, standardChargingLayout(slotsPerFloor));
    }

    public ParkingFacility(int[] slotsPerFloor, boolean[][] chargingSlots) {
        Objects.requireNonNull(slotsPerFloor, "slotsPerFloor");
        Objects.requireNonNull(chargingSlots, "chargingSlots");
        if (chargingSlots.length != slotsPerFloor.length) {
            throw new IllegalArgumentException("charging layout must have one row per floor");
        }
        floors = new ParkingSlot[slotsPerFloor.length][];
        for (int floor = 0; floor < slotsPerFloor.length; floor++) {
            if (slotsPerFloor[floor] < 0) {
                throw new IllegalArgumentException("slot count cannot be negative");
            }
            if (chargingSlots[floor] != null
                    && chargingSlots[floor].length != slotsPerFloor[floor]) {
                throw new IllegalArgumentException("charging layout does not match floor size");
            }
            floors[floor] = new ParkingSlot[slotsPerFloor[floor]];
            for (int slot = 0; slot < slotsPerFloor[floor]; slot++) {
                boolean charging = chargingSlots[floor] != null && chargingSlots[floor][slot];
                floors[floor][slot] = new ParkingSlot(floor, slot, charging);
            }
        }
    }

    public synchronized ParkingTicket park(Vehicle vehicle, LocalDateTime entryTime) {
        Objects.requireNonNull(vehicle, "vehicle");
        Objects.requireNonNull(entryTime, "entryTime");
        String regNo = vehicle.getRegNo();
        if (activeTickets.containsKey(regNo)) {
            throw new IllegalArgumentException("vehicle is already parked: " + regNo);
        }

        ParkingSlot slot = findFreeSlot(vehicle);
        if (slot == null) {
            throw new IllegalStateException("no suitable parking slot is available");
        }
        slot.park(vehicle);
        ParkingTicket ticket = new ParkingTicket(vehicle, slot, entryTime);
        activeTickets.put(regNo, ticket);
        return ticket;
    }

    public synchronized ParkingReceipt exit(String regNo, LocalDateTime exitTime, int kWh) {
        Objects.requireNonNull(regNo, "regNo");
        Objects.requireNonNull(exitTime, "exitTime");
        if (kWh < 0) {
            throw new IllegalArgumentException("kWh cannot be negative");
        }

        ParkingTicket ticket = activeTickets.get(regNo.trim().toUpperCase());
        if (ticket == null) {
            throw new IllegalArgumentException("no active ticket for registration: " + regNo);
        }
        if (exitTime.isBefore(ticket.getEntryTime())) {
            throw new IllegalArgumentException("exit time cannot be before entry time");
        }
        Vehicle vehicle = ticket.getVehicle();
        if (kWh > 0 && !(vehicle instanceof Chargeable)) {
            throw new IllegalArgumentException("charging units require a chargeable vehicle");
        }

        long minutes = Duration.between(ticket.getEntryTime(), exitTime).toMinutes();
        if (minutes > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("parking duration is too long");
        }
        double grossParkingFee = vehicle.calculateFee(ticket.getEntryTime(), (int) minutes);
        ParkingPass pass = vehicle.getPass();
        double parkingFee = pass == null ? grossParkingFee
                : pass.parkingFeeAfterPass(grossParkingFee, ticket.getEntryTime(), exitTime);
        double chargingFee = vehicle instanceof Chargeable chargeable
                ? chargeable.chargingFee(kWh) : 0.0;

        ticket.getSlot().release();
        activeTickets.remove(vehicle.getRegNo());
        return new ParkingReceipt(ticket, minutes, parkingFee, chargingFee);
    }

    public synchronized ParkingTicket findActiveTicket(String regNo) {
        Objects.requireNonNull(regNo, "regNo");
        return activeTickets.get(regNo.trim().toUpperCase());
    }

    public synchronized int getActiveVehicleCount() {
        return activeTickets.size();
    }

    public synchronized boolean isValidSlot(int floor, int slot) {
        return floor >= 0 && floor < floors.length
                && slot >= 0 && slot < floors[floor].length;
    }

    private ParkingSlot findFreeSlot(Vehicle vehicle) {
        boolean needsChargingSlot = vehicle instanceof Chargeable;
        if (needsChargingSlot) {
            ParkingSlot charging = findFreeSlot(true);
            if (charging != null) {
                return charging;
            }
        }
        return findFreeSlot(false);
    }

    private ParkingSlot findFreeSlot(boolean chargingSlot) {
        for (ParkingSlot[] floor : floors) {
            for (ParkingSlot slot : floor) {
                if (!slot.isOccupied() && slot.isChargingSlot() == chargingSlot) {
                    return slot;
                }
            }
        }
        return null;
    }

    private static boolean[][] standardChargingLayout(int[] slotsPerFloor) {
        Objects.requireNonNull(slotsPerFloor, "slotsPerFloor");
        return new boolean[slotsPerFloor.length][];
    }
}
