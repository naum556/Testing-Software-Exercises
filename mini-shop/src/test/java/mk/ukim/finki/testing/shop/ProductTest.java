package mk.ukim.finki.testing.shop;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Product record tests")
class ProductTest {

    @Nested
    @DisplayName("Constructor validation tests")
    class ConstructorValidation {

        @ParameterizedTest(name = "Code '' should throw IllegalArgumentException")
        @NullAndEmptySource
        @DisplayName("Null or empty code should throw")
        void nullOrEmptyCode_throwsException(String code) {
            assertThrows(IllegalArgumentException.class, () ->
                new Product(code, 100, 10)
            );
        }

        @ParameterizedTest(name = "Code '{0}' should throw IllegalArgumentException")
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("Blank code should throw")
        void blankCode_throwsException(String code) {
            assertThrows(IllegalArgumentException.class, () ->
                new Product(code, 100, 10)
            );
        }

        @ParameterizedTest(name = "Price {0} should throw IllegalArgumentException")
        @ValueSource(ints = {0, -1, -100, 1_000_001, 2_000_000})
        @DisplayName("Invalid price should throw")
        void invalidPrice_throwsException(int price) {
            assertThrows(IllegalArgumentException.class, () ->
                new Product("ABC", price, 10)
            );
        }

        @ParameterizedTest(name = "Quantity {0} should throw IllegalArgumentException")
        @ValueSource(ints = {-1, -100, Integer.MIN_VALUE})
        @DisplayName("Negative quantity should throw")
        void negativeQuantity_throwsException(int quantity) {
            assertThrows(IllegalArgumentException.class, () ->
                new Product("ABC", 100, quantity)
            );
        }
    }

    @Nested
    @DisplayName("Valid product creation tests")
    class ValidCreation {

        @Test
        @DisplayName("Create product with valid values")
        void createProduct_withValidValues() {
            Product product = new Product("ABC", 1000, 50);
            assertEquals("ABC", product.code());
            assertEquals(1000, product.unitPriceCents());
            assertEquals(50, product.availableQuantity());
        }

        @Test
        @DisplayName("Create product with zero quantity")
        void createProduct_withZeroQuantity() {
            Product product = new Product("ABC", 1000, 0);
            assertEquals(0, product.availableQuantity());
        }

        @Test
        @DisplayName("Create product with minimum price")
        void createProduct_withMinimumPrice() {
            Product product = new Product("ABC", 1, 10);
            assertEquals(1, product.unitPriceCents());
        }

        @Test
        @DisplayName("Create product with maximum price")
        void createProduct_withMaximumPrice() {
            Product product = new Product("ABC", 1_000_000, 10);
            assertEquals(1_000_000, product.unitPriceCents());
        }

        @Test
        @DisplayName("Product is immutable")
        void product_isImmutable() {
            Product product = new Product("ABC", 1000, 50);
            assertEquals("ABC", product.code());
            assertEquals(1000, product.unitPriceCents());
            assertEquals(50, product.availableQuantity());
        }
    }

    @Nested
    @DisplayName("Record accessor tests")
    class Accessors {

        @Test
        @DisplayName("Code accessor returns correct value")
        void code_accessorReturnsValue() {
            Product product = new Product("XYZ123", 500, 20);
            assertEquals("XYZ123", product.code());
        }

        @Test
        @DisplayName("Unit price accessor returns correct value")
        void unitPriceCents_accessorReturnsValue() {
            Product product = new Product("XYZ123", 500, 20);
            assertEquals(500, product.unitPriceCents());
        }

        @Test
        @DisplayName("Available quantity accessor returns correct value")
        void availableQuantity_accessorReturnsValue() {
            Product product = new Product("XYZ123", 500, 20);
            assertEquals(20, product.availableQuantity());
        }
    }
}
