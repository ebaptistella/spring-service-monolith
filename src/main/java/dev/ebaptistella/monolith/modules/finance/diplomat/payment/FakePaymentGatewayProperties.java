package dev.ebaptistella.monolith.modules.finance.diplomat.payment;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.finance.payment-gateway")
public record FakePaymentGatewayProperties(boolean alwaysFail) {
}
