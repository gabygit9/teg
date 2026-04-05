package ar.edu.utn.frc.tup.piii.controllers;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ping controller class to health check.
 */
class PingControllerTest {

    // ==================== UNIT TESTS ====================

    @Nested
    @ExtendWith(MockitoExtension.class)
    class UnitTests {

        @InjectMocks
        private PingController pingController;

        @Test
        void testPongMethod_ReturnsCorrectString() {
            // Given & When
            String result = pingController.pong();

            // Then
            assertNotNull(result);
            assertEquals("pong", result);
        }

        @Test
        void testPongMethod_AlwaysReturnsSameValue() {
            // Given & When
            String result1 = pingController.pong();
            String result2 = pingController.pong();
            String result3 = pingController.pong();

            // Then
            assertEquals(result1, result2);
            assertEquals(result2, result3);
            assertEquals("pong", result1);
            assertEquals("pong", result2);
            assertEquals("pong", result3);
        }

        @Test
        void testPongMethod_IsNotNull() {
            // Given & When
            String result = pingController.pong();

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertFalse(result.isBlank());
        }
    }
}