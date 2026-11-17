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
import parking.service.ParkingFacility;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

public final class ParkingDemo {
    private ParkingDemo() {
    }

    public static void main(String[] args) {
        int[] capacities = {2, 3, 2, 2, 3};
        boolean[][] charging = {
                {true, false}, {false, true, false}, {false, false},
                {true, false}, {false, false, false}
        };
        ParkingFacility facility = new ParkingFacility(capacities, charging);
        LocalDateTime entry = LocalDateTime.of(2026, 11, 17, 10, 0);

        ParkingTicket carTicket = facility.park(new Car("TN01AA1001"), entry);
        ParkingReceipt car = facility.exit("TN01AA1001", entry.plusMinutes(130), 0);
        System.out.println(carTicket.getTicketNumber() + " car fee = Rs " + car.getTotalFee());

        facility.park(new ElectricCar("TN02BB2002"), entry);
        ParkingReceipt ev = facility.exit("TN02BB2002", entry.plusMinutes(90), 10);
        System.out.println("electric car parking + charging = Rs " + ev.getTotalFee());

        facility.park(new EBike("TN03CC3003"), entry);
        System.out.println("e-bike fee = Rs "
                + facility.exit("TN03CC3003", entry.plusMinutes(30), 0).getTotalFee());

        facility.park(new DeliveryVan("TN04DD4004"), entry);
        System.out.println("delivery van fee = Rs "
                + facility.exit("TN04DD4004", entry.plusHours(1), 0).getTotalFee());

        facility.park(new Car("TN05EE5005",
                new OneDayPass(LocalDate.from(entry))), entry);
        System.out.println("visitor with one-day pass fee = Rs "
                + facility.exit("TN05EE5005", entry.plusHours(2), 0).getTotalFee());

        facility.park(new TwoWheeler("TN06FF6006",
                new MonthlyPass(YearMonth.from(entry))), entry);
        System.out.println("monthly-pass fee = Rs "
                + facility.exit("TN06FF6006", entry.plusHours(1), 0).getTotalFee());
    }
}
