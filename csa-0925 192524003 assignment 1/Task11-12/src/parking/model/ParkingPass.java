package parking.model;

import java.time.LocalDateTime;

public interface ParkingPass {
    boolean covers(LocalDateTime entry, LocalDateTime exit);

    default double parkingFeeAfterPass(double parkingFee, LocalDateTime entry, LocalDateTime exit) {
        return isCovered(this, entry, exit) ? 0.0 : parkingFee;
    }

    static boolean isCovered(ParkingPass pass, LocalDateTime entry, LocalDateTime exit) {
        return pass != null && pass.covers(entry, exit);
    }
}
