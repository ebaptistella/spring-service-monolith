package dev.ebaptistella.monolith.modules.notification.diplomat;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface NotificationPlatformApi {

    @PostExchange("/api/v1/notifications/welcome")
    void publishWelcome(
            @RequestHeader("Authorization") String authorization,
            @RequestBody NotificationWelcomeRequest body);

    @PostExchange("/api/v1/notifications/order-confirmed")
    void publishOrderConfirmed(
            @RequestHeader("Authorization") String authorization,
            @RequestBody NotificationOrderConfirmedRequest body);
}
