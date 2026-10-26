package mk.ukim.finki.testing.shop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("PriceCalculator tests")
class PriceCalculatorTest {

    private PriceCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PriceCalculator();
    }

    @Nested
    @DisplayName("Validation tests")
    class Validation {

        @ParameterizedTest(name = "Price {0} should throw IllegalArgumentException")
        @ValueSource(ints = {0, -1, -100, 1_000_001, 2_000_000, Integer.MAX_VALUE})
        @DisplayName("Invalid unit price should throw")
        void invalidPrice_throwsException(int price) {
            assertThrows(IllegalArgumentException.class, () ->
                calculator.calculateTotal(price, 1, false)
            );
        }

        @ParameterizedTest(name = "Quantity {0} should throw IllegalArgumentException")
        @ValueSource(ints = {0, -1, -100, 101, 200, Integer.MAX_VALUE})
        @DisplayName("Invalid quantity should throw")
        void invalidQuantity_throwsException(int quantity) {
            assertThrows(IllegalArgumentException.class, () ->
                calculator.calculateTotal(100, quantity, false)
            );
        }

        @Test
        @DisplayName("Boundary prices should be accepted")
        void boundaryPrices_areAccepted() {
            assertEquals(501, calculator.calculateTotal(1, 1, false));
            assertEquals(1_000_000, calculator.calculateTotal(1_000_000, 1, false));
        }

        @Test
        @DisplayName("Boundary quantities should be accepted")
        void boundaryQuantities_areAccepted() {
            assertEquals(600, calculator.calculateTotal(100, 1, false));
            assertEquals(10_000, calculator.calculateTotal(100, 100, false));
        }
    }

    @Nested
    @DisplayName("Standard customer tests")
    class StandardCustomer {

        @Test
        @DisplayName("Single item with shipping")
        void singleItem_withShipping() {
            int total = calculator.calculateTotal(1000, 1, false);
            assertEquals(1500, total);
        }

        @Test
        @DisplayName("Multiple items with shipping")
        void multipleItems_withShipping() {
            int total = calculator.calculateTotal(1000, 3, false);
            assertEquals(3500, total);
        }

        @Test
        @DisplayName("Order at free shipping threshold")
        void orderAtThreshold_freeShipping() {
            int total = calculator.calculateTotal(1000, 5, false);
            assertEquals(5000, total);
        }

        @Test
        @DisplayName("Order above free shipping threshold")
        void orderAboveThreshold_freeShipping() {
            int total = calculator.calculateTotal(1000, 6, false);
            assertEquals(6000, total);
        }

        @ParameterizedTest(name = "Price {0}, Qty {1} -> Total {2}")
        @CsvSource({
            "1, 1, 501", // min valid price
            "100, 10, 1500", // regular valid price
            "500, 10, 5000", // checks free shipping boundary
            "1000, 5, 5000", // checks free shipping boundary
            "1000, 4, 4500", // below shipping threshold
            "10000, 1, 10000" // above shipping threshold
        })
        @DisplayName("Various standard customer scenarios")
        void variousScenarios(int price, int qty, int expected) {
            assertEquals(expected, calculator.calculateTotal(price, qty, false));
        }
    }

    @Nested
    @DisplayName("Premium customer tests")
    class PremiumCustomer {

        @Test
        @DisplayName("Single item with discount and shipping")
        void singleItem_withDiscountAndShipping() {
            int total = calculator.calculateTotal(1000, 1, true);
            assertEquals(1400, total);
        }

        @Test
        @DisplayName("Order at free shipping threshold with discount")
        void orderAtThreshold_withDiscount() {
            int total = calculator.calculateTotal(1000, 6, true);
            assertEquals(5400, total);
        }

        @Test
        @DisplayName("Order below free shipping threshold with discount")
        void orderBelowThreshold_withDiscount() {
            int total = calculator.calculateTotal(1000, 5, true);
            assertEquals(5000, total);
        }

        @Test
        @DisplayName("Discount rounding down")
        void discountRoundsDown() {
            int total = calculator.calculateTotal(333, 3, true);
            int subtotal = 333 * 3;
            int discounted = (int) (subtotal * 90L / 100);
            assertEquals(discounted + 500, total);
        }

        @ParameterizedTest(name = "Price {0}, Qty {1} -> Total {2}")
        @CsvSource({
            "1000, 1, 1400", // shipping applied
            "1000, 5, 5000", // shipping applied
            "1000, 6, 5400", // no shipping
            "10000, 1, 9000", // no shipping
            "100, 10, 1400" // shipping applied
        })
        @DisplayName("Various premium customer scenarios")
        void variousScenarios(int price, int qty, int expected) {
            assertEquals(expected, calculator.calculateTotal(price, qty, true));
        }
    }

    @Nested
    @DisplayName("Shipping threshold tests")
    class ShippingThreshold {

        @Test
        @DisplayName("Standard customer just below threshold")
        void standardJustBelowThreshold() {
            int total = calculator.calculateTotal(999, 5, false);
            assertEquals(5495, total);
        }

        @Test
        @DisplayName("Standard customer at threshold")
        void standardAtThreshold() {
            int total = calculator.calculateTotal(1000, 5, false);
            assertEquals(5000, total);
        }

        @Test
        @DisplayName("Premium customer just below threshold")
        void premiumJustBelowThreshold() {
            int total = calculator.calculateTotal(1111, 5, true);
            int subtotal = 1111 * 5;
            int discounted = (int) (subtotal * 90L / 100);
            assertEquals(discounted + 500, total);
        }

        @Test
        @DisplayName("Premium customer at threshold")
        void premiumAtThreshold() {
            int total = calculator.calculateTotal(1112, 5, true);
            int subtotal = 1112 * 5;
            int discounted = (int) (subtotal * 90L / 100);
            assertEquals(discounted, total);
        }
    }
}
