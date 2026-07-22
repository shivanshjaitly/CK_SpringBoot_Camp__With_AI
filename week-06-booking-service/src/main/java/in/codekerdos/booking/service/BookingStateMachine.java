package in.codekerdos.booking.service;

import in.codekerdos.booking.enums.BookingStatus;
import in.codekerdos.booking.exception.BusinessException;

public final class BookingStateMachine {

    private BookingStateMachine() {}

    public static void assertTransition(BookingStatus from, BookingStatus to) {
        boolean ok = (from == BookingStatus.PENDING && to == BookingStatus.CONFIRMED)
                || (from == BookingStatus.PENDING && to == BookingStatus.CANCELLED)
                || (from == BookingStatus.CONFIRMED && to == BookingStatus.CANCELLED);
        if (!ok) {
            throw new BusinessException("Illegal transition: " + from + " → " + to);
        }
    }
}
