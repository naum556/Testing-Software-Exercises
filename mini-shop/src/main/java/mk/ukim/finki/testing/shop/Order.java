package mk.ukim.finki.testing.shop;

/**
 * Immutable order data created by OrderService. All amounts are cents.
 *
 * @param customerId identifier of the customer who placed the order
 * @param productCode code of the ordered product
 * @param quantity number of units ordered
 * @param totalCents total price in cents including discounts and shipping
 */
public record Order(String customerId, String productCode, int quantity, int totalCents) {
}
