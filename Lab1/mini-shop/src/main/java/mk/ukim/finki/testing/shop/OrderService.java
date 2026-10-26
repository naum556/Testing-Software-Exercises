package mk.ukim.finki.testing.shop;

import java.util.Objects;

/** Service that orchestrates placing an order through the shop. */
public class OrderService {

    private static final int MIN_QUANTITY = 1;
    private static final int MAX_QUANTITY = 100;

    private final ProductRepository products;
    private final OrderRepository orders;
    private final OrderNotifier notifier;
    private final PriceCalculator calculator;

    /**
     * Creates a new OrderService.
     *
     * @param products repository for looking up products
     * @param orders repository for saving orders
     * @param notifier service for sending order confirmations
     * @param calculator service for calculating order totals
     * @throws NullPointerException if any dependency is null
     */
    public OrderService(ProductRepository products, OrderRepository orders,
                        OrderNotifier notifier, PriceCalculator calculator) {
        this.products = Objects.requireNonNull(products);
        this.orders = Objects.requireNonNull(orders);
        this.notifier = Objects.requireNonNull(notifier);
        this.calculator = Objects.requireNonNull(calculator);
    }

    /**
     * Places an order for a customer.
     *
     * @param customerId identifier of the customer (must not be blank)
     * @param productCode code of the product to order (must not be blank)
     * @param quantity number of units to order (1–100)
     * @param premiumCustomer true if the customer receives a premium discount
     * @return the created order with the calculated total
     * @throws IllegalArgumentException if the input is invalid
     * @throws ProductNotFoundException if the product code does not exist
     * @throws InsufficientStockException if the requested quantity exceeds available stock
     * @throws IllegalStateException if the order cannot be saved or notified
     */
    public Order placeOrder(String customerId, String productCode,
                            int quantity, boolean premiumCustomer) {
        validateCustomerId(customerId);
        validateProductCode(productCode);
        validateQuantity(quantity);

        Product product = products.findByCode(productCode)
                .orElseThrow(() -> new ProductNotFoundException(productCode));
        if (quantity > product.availableQuantity()) {
            throw new InsufficientStockException(quantity, product.availableQuantity());
        }

        int total = calculator.calculateTotal(product.unitPriceCents(), quantity, premiumCustomer);
        Order order = new Order(customerId, productCode, quantity, total);
        orders.save(order);
        notifier.sendConfirmation(order);
        return order;
    }

    private void validateCustomerId(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID must not be blank");
        }
    }

    private void validateProductCode(String productCode) {
        if (productCode == null || productCode.isBlank()) {
            throw new IllegalArgumentException("Product code must not be blank");
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("Quantity must be between 1 and 100");
        }
    }
}
