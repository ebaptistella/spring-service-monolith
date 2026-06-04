package dev.ebaptistella.monolith.shared.resilience;

import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;

@Component
public class NotificationPlatformConcurrencyLimiter {

    private static final int LIMIT = 5;

    private final Semaphore semaphore = new Semaphore(LIMIT);

    public void run(Runnable action) {
        try {
            semaphore.acquire();
            try {
                action.run();
            } finally {
                semaphore.release();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted waiting for notification platform slot", ex);
        }
    }
}
