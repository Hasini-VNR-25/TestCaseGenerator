package samples;

/**
 * Dependency interface for notification alerts.
 */
public interface NotificationService {
    boolean sendLowStockAlert(String productId, int currentStock);
    void logOperation(String operationName, String details);
}
