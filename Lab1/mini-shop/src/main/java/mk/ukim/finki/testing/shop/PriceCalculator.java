package mk.ukim.finki.testing.shop;

/** Calculates the total for an order containing one product type. All amounts are cents. */
public class PriceCalculator {

    private static final int MIN_PRICE = 1;
    private static final int MAX_PRICE = 1_000_000;
    private static final int MIN_QUANTITY = 1;
    private static final int MAX_QUANTITY = 100;
    private static final int PREMIUM_DISCOUNT_PERCENT = 90;
    private static final int DISCOUNT_DIVISOR = 100;
    private static final int SHIPPING_COST = 500;
    private static final int FREE_SHIPPING_THRESHOLD = 5_000;

    /**
     * Calculates the total price for an order.
     *
     * @param unitPriceCents price of a single unit in cents (1–1,000,000)
     * @param quantity number of units (1–100)
     * @param premiumCustomer true if the customer receives a 10% discount
     * @return total price in cents including any applicable shipping
     * @throws IllegalArgumentException if price or quantity is out of range
     */
    public int calculateTotal(int unitPriceCents, int quantity, boolean premiumCustomer) {
        validatePrice(unitPriceCents);
        validateQuantity(quantity);

        int subtotal = unitPriceCents * quantity;
        int discountedSubtotal = calculateDiscount(subtotal, premiumCustomer);
        int shipping = calculateShipping(discountedSubtotal);

        return discountedSubtotal + shipping;
    }

    private void validatePrice(int unitPriceCents) {
        if (unitPriceCents < MIN_PRICE || unitPriceCents > MAX_PRICE) {
            throw new IllegalArgumentException("Unit price must be between 1 and 1000000 cents");
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("Quantity must be between 1 and 100");
        }
    }

    private int calculateDiscount(int subtotal, boolean premiumCustomer) {
        if (premiumCustomer) {
            return subtotal * PREMIUM_DISCOUNT_PERCENT / DISCOUNT_DIVISOR;
        }
        return subtotal;
    }

    private int calculateShipping(int discountedSubtotal) {
        if (discountedSubtotal >= FREE_SHIPPING_THRESHOLD) {
            return 0;
        }
        return SHIPPING_COST;
    }
}
