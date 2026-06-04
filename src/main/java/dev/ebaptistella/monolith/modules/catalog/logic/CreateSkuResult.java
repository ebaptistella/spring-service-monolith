package dev.ebaptistella.monolith.modules.catalog.logic;

import java.util.Optional;

public record CreateSkuResult(
        Optional<CreatedSku> created,
        Optional<String> error,
        boolean replay
) {

    public static CreateSkuResult success(CreatedSku created) {
        return new CreateSkuResult(Optional.of(created), Optional.empty(), false);
    }

    public static CreateSkuResult replay(CreatedSku created) {
        return new CreateSkuResult(Optional.of(created), Optional.empty(), true);
    }

    public static CreateSkuResult rejected(String error) {
        return new CreateSkuResult(Optional.empty(), Optional.of(error), false);
    }

    public boolean rejected() {
        return error.isPresent();
    }
}
