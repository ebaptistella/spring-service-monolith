package dev.ebaptistella.monolith.modules.notification.logic;

import dev.ebaptistella.monolith.modules.notification.models.AccountStatusChangedNotification;
import dev.ebaptistella.monolith.modules.notification.models.AccountStatusEmailContent;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class AccountStatusRulesTest {

    @Test
    void buildEmailContent_includesStatus() {
        AccountStatusChangedNotification notification =
                new AccountStatusChangedNotification(UUID.randomUUID(), UUID.randomUUID(), "user@example.com", "SUSPENDED");

        AccountStatusEmailContent content = AccountStatusRules.buildEmailContent(notification);

        assertThat(content.to()).isEqualTo("user@example.com");
        assertThat(content.subject()).isEqualTo("Your account status was updated");
        assertThat(content.body()).contains("SUSPENDED");
    }
}
