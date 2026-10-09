package samples;

public interface NotificationService {
    boolean sendLowStockAlert(String productId, int currentStock);
    void logOperation(String operationName, String details);
}
