package dev.ebaptistella.monolith.modules.finance.diplomat.payment;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@EnableConfigurationProperties(FakePaymentGatewayProperties.class)
public class FakePaymentGateway implements PaymentGateway {

    private final FakePaymentGatewayProperties properties;

    public FakePaymentGateway(FakePaymentGatewayProperties properties) {
        this.properties = properties;
    }

    @Override
    public PaymentGatewayCaptureResult capture(UUID orderId, BigDecimal amount) {
        if (properties.alwaysFail()) {
            return PaymentGatewayCaptureResult.failure("Simulated gateway failure");
        }
        return PaymentGatewayCaptureResult.success("fake-" + orderId);
    }
}
