package samples;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Inventory Manager demonstrating OOP inheritance (extends BaseManager),
 * dependency injection with Mockito interfaces, and collection state management.
 */
public class InventoryManager extends BaseManager {
    private final NotificationService notificationService;
    private final ProductRepository productRepository;
    private final Map<String, Integer> localCache;
    private int lowStockThreshold;

    public InventoryManager(String managerName, NotificationService notificationService, ProductRepository productRepository) {
        super(managerName);
        if (notificationService == null || productRepository == null) {
            throw new IllegalArgumentException("Collaborator services cannot be null");
        }
        this.notificationService = notificationService;
        this.productRepository = productRepository;
        this.localCache = new HashMap<>();
        this.lowStockThreshold = 5;
    }

    public int checkStock(String productId) {
        if (!isActive()) {
            throw new IllegalStateException("Manager is inactive");
        }
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        if (localCache.containsKey(productId)) {
            return localCache.get(productId);
        }
        Optional<Integer> repoStock = productRepository.findStockByProductId(productId);
        int stock = repoStock.orElse(0);
        localCache.put(productId, stock);
        return stock;
    }

    public boolean restockProduct(String productId, int quantity) {
        if (!isActive()) {
            throw new IllegalStateException("Manager is inactive");
        }
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Restock quantity must be positive");
        }
        int currentStock = checkStock(productId);
        int updated = currentStock + quantity;
        boolean saved = productRepository.updateStock(productId, updated);
        if (saved) {
            localCache.put(productId, updated);
            notificationService.logOperation("RESTOCK", "Product " + productId + " restocked by " + quantity);
            return true;
        }
        return false;
    }

    public boolean dispatchProduct(String productId, int quantity) {
        if (!isActive()) {
            throw new IllegalStateException("Manager is inactive");
        }
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Dispatch quantity must be positive");
        }
        int currentStock = checkStock(productId);
        if (currentStock < quantity) {
            return false;
        }
        int updated = currentStock - quantity;
        boolean saved = productRepository.updateStock(productId, updated);
        if (saved) {
            localCache.put(productId, updated);
            if (updated <= lowStockThreshold) {
                notificationService.sendLowStockAlert(productId, updated);
            }
            notificationService.logOperation("DISPATCH", "Product " + productId + " dispatched: " + quantity);
            return true;
        }
        return false;
    }

    @Override
    public String getStatusReport() {
        return "InventoryManager [" + getManagerName() + "] Active: " + isActive() + ", Cached Items: " + localCache.size();
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(int lowStockThreshold) {
        if (lowStockThreshold < 0) {
            throw new IllegalArgumentException("Threshold cannot be negative");
        }
        this.lowStockThreshold = lowStockThreshold;
    }
}
