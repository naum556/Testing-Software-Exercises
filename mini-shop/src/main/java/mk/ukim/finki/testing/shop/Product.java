package mk.ukim.finki.testing.shop;

/**
 * Product data returned by the catalog. Stock is a snapshot, not a reservation.
 *
 * @param code unique product identifier (must not be blank)
 * @param unitPriceCents price per unit in cents (1–1,000,000)
 * @param availableQuantity number of units available (zero or positive)
 */
public record Product(String code, int unitPriceCents, int availableQuantity) {

    private static final int MIN_PRICE = 1;
    private static final int MAX_PRICE = 1_000_000;

    public Product {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Product code must not be blank");
        }
        if (unitPriceCents < MIN_PRICE || unitPriceCents > MAX_PRICE) {
            throw new IllegalArgumentException("Unit price must be between 1 and 1000000 cents");
        }
        if (availableQuantity < 0) {
            throw new IllegalArgumentException("Available quantity must not be negative");
        }
    }
}
