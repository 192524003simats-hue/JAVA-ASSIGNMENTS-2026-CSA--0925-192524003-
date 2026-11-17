package parking.app;

import parking.model.Car;
import parking.model.DeliveryVan;
import parking.model.EBike;
import parking.model.ElectricCar;
import parking.model.MonthlyPass;
import parking.model.OneDayPass;
import parking.model.ParkingReceipt;
import parking.model.ParkingTicket;
import parking.model.TwoWheeler;
import parking.model.Vehicle;
import parking.model.VehicleFactory;
import parking.service.ParkingFacility;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.Set;

public final class AssignmentChecks {
    private static int passed;

    private AssignmentChecks() {
    }

    public static void main(String[] args) {
        feeBoundaries();
        normalParkingAndTicketing();
        chargingAndSuitableSlots();
        passesAndNewVehicleTypes();
        invalidInputs();
        dailyCollection();
        equalityContract();
        System.out.println("PASS: " + passed + " acceptance checks");
    }

    private static void feeBoundaries() {
        LocalDateTime entry = LocalDateTime.of(2026, 11, 17, 8, 0);
        equal(0.0, new Car("BOUNDARY0").calculateFee(entry, 0), "zero minutes");
        equal(30.0, new Car("BOUNDARY60").calculateFee(entry, 60), "exactly one hour");
        equal(60.0, new Car("BOUNDARY61").calculateFee(entry, 61), "one minute over");
        equal(90.0, new Car("BOUNDARY130").calculateFee(entry, 130), "130 minutes");
    }

    private static void normalParkingAndTicketing() {
        ParkingFacility facility = new ParkingFacility(new int[]{1, 1});
        LocalDateTime entry = LocalDateTime.of(2026, 11, 17, 8, 0);
        ParkingTicket first = facility.park(new TwoWheeler("tn01aa0001"), entry);
        ParkingTicket second = facility.park(new Car("TN02BB0002"), entry);
        check(!first.getTicketNumber().equals(second.getTicketNumber()), "unique tickets");
        check(first.getSlot().getFloorNumber() == 0 && second.getSlot().getFloorNumber() == 1,
                "first-free allocation across floors");
        expect(IllegalStateException.class,
                () -> facility.park(new Car("TN03CC0003"), entry), "last slot/full facility");
        ParkingReceipt receipt = facility.exit("TN02BB0002", entry.plusHours(2), 0);
        equal(60.0, receipt.getTotalFee(), "visitor car pays full fee");
        check(facility.getActiveVehicleCount() == 1, "exit releases vehicle");
    }

    private static void chargingAndSuitableSlots() {
        ParkingFacility facility = new ParkingFacility(new int[]{2},
                new boolean[][]{{false, true}});
        LocalDateTime entry = LocalDateTime.of(2026, 11, 17, 10, 0);
        ParkingTicket ev = facility.park(new ElectricCar("TN10EV0010"), entry);
        check(ev.getSlot().isChargingSlot(), "EV prefers a charging slot");
        ParkingTicket car = facility.park(new Car("TN11CA0011"), entry);
        check(!car.getSlot().isChargingSlot(), "non-EV uses a standard slot");
        ParkingReceipt receipt = facility.exit("TN10EV0010", entry.plusMinutes(90), 10);
        equal(180.0, receipt.getTotalFee(), "EV parking and charging fee");
        equal(120.0, receipt.getChargingFee(), "charging is separately billed");
    }

    private static void passesAndNewVehicleTypes() {
        LocalDateTime entry = LocalDateTime.of(2026, 11, 17, 10, 0);
        ParkingFacility facility = new ParkingFacility(new int[]{5});
        facility.park(new Car("TN20PASS01", new OneDayPass(entry.toLocalDate())), entry);
        equal(0.0, facility.exit("TN20PASS01", entry.plusHours(2), 0).getTotalFee(),
                "visitor one-day pass waives parking");
        facility.park(new TwoWheeler("TN21PASS02",
                new MonthlyPass(YearMonth.from(entry))), entry);
        equal(0.0, facility.exit("TN21PASS02", entry.plusHours(1), 0).getTotalFee(),
                "valid monthly pass waives parking");
        facility.park(new EBike("TN22EB0001"), entry);
        equal(25.0, facility.exit("TN22EB0001", entry.plusMinutes(1), 0).getTotalFee(),
                "e-bike flat visit fee");
        facility.park(new DeliveryVan("TN23DV0001"), entry);
        equal(60.0, facility.exit("TN23DV0001", entry.plusHours(1), 0).getTotalFee(),
                "delivery van doubled daytime rate");
        facility.park(new DeliveryVan("TN24DV0002"),
                LocalDateTime.of(2026, 11, 17, 8, 0));
        equal(90.0, facility.exit("TN24DV0002",
                LocalDateTime.of(2026, 11, 17, 10, 0), 0).getTotalFee(),
                "van rate changes at 9 AM");
    }

    private static void invalidInputs() {
        LocalDateTime entry = LocalDateTime.of(2026, 11, 17, 8, 0);
        ParkingFacility facility = new ParkingFacility(new int[]{1});
        facility.park(new Car("TN30CAR001"), entry);
        expect(IllegalArgumentException.class,
                () -> facility.park(new Car("TN30CAR001"), entry), "duplicate registration");
        expect(IllegalArgumentException.class,
                () -> VehicleFactory.create("TN31UNK001", "spaceship", null),
                "unknown vehicle type");
        expect(IllegalArgumentException.class,
                () -> facility.exit("TN30CAR001", entry.minusMinutes(1), 0),
                "exit before entry");
        expect(IllegalArgumentException.class,
                () -> facility.exit("TN30CAR001", entry.plusHours(1), 2),
                "charging a non-EV");
        expect(IllegalArgumentException.class,
                () -> facility.exit("TN99NONE00", entry, 0), "unknown active ticket");
        check(!facility.isValidSlot(-1, 0) && !facility.isValidSlot(0, 1),
                "invalid floor and slot indexes");
        expect(IllegalArgumentException.class,
                () -> new ParkingFacility(new int[]{2}, new boolean[][]{{true}}),
                "invalid charging-slot layout");
    }

    private static void dailyCollection() {
        LocalDateTime entry = LocalDateTime.of(2026, 11, 17, 8, 0);
        ParkingFacility facility = new ParkingFacility(new int[]{2});
        facility.park(new Car("TN40DAY001"), entry);
        double carCollection = facility.exit("TN40DAY001", entry.plusHours(2), 0).getTotalFee();
        facility.park(new TwoWheeler("TN41DAY002"), entry);
        double bikeCollection = facility.exit("TN41DAY002", entry.plusHours(1), 0).getTotalFee();
        equal(70.0, carCollection + bikeCollection, "hand-calculated daily collection");
    }

    private static void equalityContract() {
        Vehicle car = new Car("TN50SAME01");
        Vehicle ev = new ElectricCar("TN50SAME01");
        check(car.equals(ev), "same registration compares equal across subclasses");
        Set<Vehicle> vehicles = new HashSet<>();
        vehicles.add(car);
        vehicles.add(ev);
        check(vehicles.size() == 1, "equals and hashCode contract");
    }

    private static void equal(double expected, double actual, String label) {
        if (Math.abs(expected - actual) > 0.000001) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
        passed++;
    }

    private static void check(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError(label);
        }
        passed++;
    }

    private static void expect(Class<? extends RuntimeException> expected,
                               Runnable operation, String label) {
        try {
            operation.run();
        } catch (RuntimeException exception) {
            if (expected.isInstance(exception)) {
                passed++;
                return;
            }
            throw new AssertionError(label + ": expected " + expected.getSimpleName()
                    + ", got " + exception.getClass().getSimpleName(), exception);
        }
        throw new AssertionError(label + ": expected " + expected.getSimpleName());
    }
}
