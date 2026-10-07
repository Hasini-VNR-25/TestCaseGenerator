package samples;

import java.util.Optional;

/**
 * Dependency interface for product storage operations.
 */
public interface ProductRepository {
    Optional<Integer> findStockByProductId(String productId);
    boolean updateStock(String productId, int newStock);
    boolean existsById(String productId);
}
