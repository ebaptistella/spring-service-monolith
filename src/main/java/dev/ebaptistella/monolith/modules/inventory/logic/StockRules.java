package dev.ebaptistella.monolith.modules.inventory.logic;

public final class StockRules {

    private StockRules() {
    }

    public static int available(int onHand, int reserved) {
        return onHand - reserved;
    }

    public static boolean canReserve(int available, int quantity) {
        return quantity > 0 && available >= quantity;
    }

    public static boolean isValidAdjustmentResult(int resultingOnHand) {
        return resultingOnHand >= 0;
    }
}
