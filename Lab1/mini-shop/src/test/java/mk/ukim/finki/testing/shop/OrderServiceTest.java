package mk.ukim.finki.testing.shop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService tests")
class OrderServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderNotifier orderNotifier;

    private PriceCalculator priceCalculator;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        priceCalculator = new PriceCalculator();
        orderService = new OrderService(productRepository, orderRepository, orderNotifier, priceCalculator);
    }

    @Nested
    @DisplayName("Constructor validation tests")
    class ConstructorValidation {

        @Test
        @DisplayName("Null product repository should throw")
        void nullProductRepository_throwsNullPointerException() {
            assertThrows(NullPointerException.class, () ->
                new OrderService(null, orderRepository, orderNotifier, priceCalculator)
            );
        }

        @Test
        @DisplayName("Null order repository should throw")
        void nullOrderRepository_throwsNullPointerException() {
            assertThrows(NullPointerException.class, () ->
                new OrderService(productRepository, null, orderNotifier, priceCalculator)
            );
        }

        @Test
        @DisplayName("Null notifier should throw")
        void nullNotifier_throwsNullPointerException() {
            assertThrows(NullPointerException.class, () ->
                new OrderService(productRepository, orderRepository, null, priceCalculator)
            );
        }

        @Test
        @DisplayName("Null calculator should throw")
        void nullCalculator_throwsNullPointerException() {
            assertThrows(NullPointerException.class, () ->
                new OrderService(productRepository, orderRepository, orderNotifier, null)
            );
        }
    }

    @Nested
    @DisplayName("Input validation tests")
    class InputValidation {

        @ParameterizedTest(name = "Customer ID '' should throw")
        @NullAndEmptySource
        @DisplayName("Null or empty customer ID should throw")
        void nullOrEmptyCustomerId_throwsException(String customerId) {
            assertThrows(IllegalArgumentException.class, () ->
                orderService.placeOrder(customerId, "ABC", 1, false)
            );
        }

        @ParameterizedTest(name = "Customer ID '{0}' should throw")
        @ValueSource(strings = {" ", "  ", "\t"})
        @DisplayName("Blank customer ID should throw")
        void blankCustomerId_throwsException(String customerId) {
            assertThrows(IllegalArgumentException.class, () ->
                orderService.placeOrder(customerId, "ABC", 1, false)
            );
        }

        @ParameterizedTest(name = "Product code '' should throw")
        @NullAndEmptySource
        @DisplayName("Null or empty product code should throw")
        void nullOrEmptyProductCode_throwsException(String productCode) {
            assertThrows(IllegalArgumentException.class, () ->
                orderService.placeOrder("CUST1", productCode, 1, false)
            );
        }

        @ParameterizedTest(name = "Product code '{0}' should throw")
        @ValueSource(strings = {" ", "  ", "\t"})
        @DisplayName("Blank product code should throw")
        void blankProductCode_throwsException(String productCode) {
            assertThrows(IllegalArgumentException.class, () ->
                orderService.placeOrder("CUST1", productCode, 1, false)
            );
        }

        @ParameterizedTest(name = "Quantity {0} should throw")
        @ValueSource(ints = {0, -1, -100, 101, 200})
        @DisplayName("Invalid quantity should throw")
        void invalidQuantity_throwsException(int quantity) {
            assertThrows(IllegalArgumentException.class, () ->
                orderService.placeOrder("CUST1", "ABC", quantity, false)
            );
        }

        @Test
        @DisplayName("Validation order: customer ID checked first")
        void validationOrder_customerIdFirst() {
            assertThrows(IllegalArgumentException.class, () ->
                orderService.placeOrder(null, null, 0, false)
            );
        }
    }

    @Nested
    @DisplayName("Product lookup tests")
    class ProductLookup {

        @Test
        @DisplayName("Missing product should throw ProductNotFoundException")
        void missingProduct_throwsProductNotFoundException() {
            when(productRepository.findByCode("UNKNOWN")).thenReturn(Optional.empty());

            assertThrows(ProductNotFoundException.class, () ->
                orderService.placeOrder("CUST1", "UNKNOWN", 1, false)
            );

            verify(orderRepository, never()).save(any()); // verify that save is never called
            verify(orderNotifier, never()).sendConfirmation(any()); // verify that sendConfirmation is never called
        }

        @Test
        @DisplayName("Product lookup is called with exact code")
        void productLookup_calledWithExactCode() {
            Product product = new Product("ABC", 1000, 10);
            when(productRepository.findByCode("ABC")).thenReturn(Optional.of(product));

            orderService.placeOrder("CUST1", "ABC", 1, false);

            verify(productRepository).findByCode("ABC");
        }
    }

    @Nested
    @DisplayName("Stock validation tests")
    class StockValidation {

        @Test
        @DisplayName("Insufficient stock should throw")
        void insufficientStock_throwsInsufficientStockException() {
            Product product = new Product("ABC", 1000, 5);
            when(productRepository.findByCode("ABC")).thenReturn(Optional.of(product));

            assertThrows(InsufficientStockException.class, () ->
                orderService.placeOrder("CUST1", "ABC", 10, false)
            );

            verify(orderRepository, never()).save(any());
            verify(orderNotifier, never()).sendConfirmation(any());
        }

        @Test
        @DisplayName("Exact available quantity is allowed")
        void exactAvailableQuantity_isAllowed() {
            Product product = new Product("ABC", 1000, 5);
            when(productRepository.findByCode("ABC")).thenReturn(Optional.of(product));

            Order order = orderService.placeOrder("CUST1", "ABC", 5, false);

            assertEquals(5, order.quantity());
            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("Zero stock should throw when quantity > 0")
        void zeroStock_throwsInsufficientStockException() {
            Product product = new Product("ABC", 1000, 0);
            when(productRepository.findByCode("ABC")).thenReturn(Optional.of(product));

            assertThrows(InsufficientStockException.class, () ->
                orderService.placeOrder("CUST1", "ABC", 1, false)
            );
        }
    }

    @Nested
    @DisplayName("Successful order tests")
    class SuccessfulOrder {

        @Test
        @DisplayName("Standard customer order")
        void standardCustomer_orderSuccess() {
            Product product = new Product("ABC", 1000, 10);
            when(productRepository.findByCode("ABC")).thenReturn(Optional.of(product));

            Order order = orderService.placeOrder("CUST1", "ABC", 2, false);

            assertEquals("CUST1", order.customerId());
            assertEquals("ABC", order.productCode());
            assertEquals(2, order.quantity());
            assertEquals(2500, order.totalCents());

            verify(orderRepository).save(order);
            verify(orderNotifier).sendConfirmation(order);
        }

        @Test
        @DisplayName("Premium customer order")
        void premiumCustomer_orderSuccess() {
            Product product = new Product("ABC", 1000, 10);
            when(productRepository.findByCode("ABC")).thenReturn(Optional.of(product));

            Order order = orderService.placeOrder("CUST1", "ABC", 2, true);

            assertEquals(2300, order.totalCents());
            verify(orderRepository).save(order);
            verify(orderNotifier).sendConfirmation(order);
        }

        @Test
        @DisplayName("Order with free shipping")
        void orderWithFreeShipping() {
            Product product = new Product("ABC", 1000, 10);
            when(productRepository.findByCode("ABC")).thenReturn(Optional.of(product));

            Order order = orderService.placeOrder("CUST1", "ABC", 5, false);

            assertEquals(5000, order.totalCents());
        }

        @Test
        @DisplayName("Order is saved and notified exactly once")
        void order_savedAndNotifiedOnce() {
            Product product = new Product("ABC", 1000, 10);
            when(productRepository.findByCode("ABC")).thenReturn(Optional.of(product));

            orderService.placeOrder("CUST1", "ABC", 1, false);

            verify(orderRepository, times(1)).save(any());
            verify(orderNotifier, times(1)).sendConfirmation(any());
        }

        @Test
        @DisplayName("Same order object is saved and notified")
        void sameOrder_savedAndNotified() {
            Product product = new Product("ABC", 1000, 10);
            when(productRepository.findByCode("ABC")).thenReturn(Optional.of(product));

            ArgumentCaptor<Order> saveCaptor = ArgumentCaptor.forClass(Order.class);
            ArgumentCaptor<Order> notifyCaptor = ArgumentCaptor.forClass(Order.class);

            orderService.placeOrder("CUST1", "ABC", 1, false);

            verify(orderRepository).save(saveCaptor.capture());
            verify(orderNotifier).sendConfirmation(notifyCaptor.capture());

            assertEquals(saveCaptor.getValue(), notifyCaptor.getValue());
        }
    }

    @Nested
    @DisplayName("Error handling tests")
    class ErrorHandling {

        @Test
        @DisplayName("Save failure should propagate and not notify")
        void saveFailure_propagatesAndDoesNotNotify() {
            Product product = new Product("ABC", 1000, 10);
            when(productRepository.findByCode("ABC")).thenReturn(Optional.of(product));
            doThrow(new IllegalStateException("Database error")).when(orderRepository).save(any());

            assertThrows(IllegalStateException.class, () ->
                orderService.placeOrder("CUST1", "ABC", 1, false)
            );

            verify(orderNotifier, never()).sendConfirmation(any());
        }

        @Test
        @DisplayName("Notification failure should propagate after save")
        void notificationFailure_propagatesAfterSave() {
            Product product = new Product("ABC", 1000, 10);
            when(productRepository.findByCode("ABC")).thenReturn(Optional.of(product));
            doThrow(new IllegalStateException("Email error")).when(orderNotifier).sendConfirmation(any());

            assertThrows(IllegalStateException.class, () ->
                orderService.placeOrder("CUST1", "ABC", 1, false)
            );

            verify(orderRepository).save(any());
        }
    }
}
