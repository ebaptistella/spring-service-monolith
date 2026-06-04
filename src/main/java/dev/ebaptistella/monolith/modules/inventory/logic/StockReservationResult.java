package dev.ebaptistella.monolith.modules.inventory.logic;

import java.util.Optional;

public record StockReservationResult(ReservationOutcome outcome, Optional<String> failureReason) {

    public enum ReservationOutcome {
        SUCCESS,
        IDEMPOTENT,
        FAILED
    }

    public boolean failed() {
        return outcome == ReservationOutcome.FAILED;
    }

    public boolean idempotent() {
        return outcome == ReservationOutcome.IDEMPOTENT;
    }

    public static StockReservationResult success() {
        return new StockReservationResult(ReservationOutcome.SUCCESS, Optional.empty());
    }

    public static StockReservationResult idempotentSuccess() {
        return new StockReservationResult(ReservationOutcome.IDEMPOTENT, Optional.empty());
    }

    public static StockReservationResult failed(String reason) {
        return new StockReservationResult(ReservationOutcome.FAILED, Optional.of(reason));
    }
}
