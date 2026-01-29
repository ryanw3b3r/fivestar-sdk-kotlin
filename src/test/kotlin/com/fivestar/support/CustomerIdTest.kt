package com.fivestar.support

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CustomerIdTest {

    // MARK: - Customer ID Tests

    @Test
    fun `test generateCustomerId produces 26 characters`() {
        val customerId = generateCustomerId("test-client-123")
        assertEquals(26, customerId.length, "Customer ID should be 26 characters")
    }

    @Test
    fun `test generated customer ID has valid format`() {
        val customerId = generateCustomerId("test-client-123")
        assertTrue(isValidCustomerIdFormat(customerId), "Generated ID should be valid format")
    }

    @Test
    fun `test verifyCustomerId with correct client`() {
        val clientId = "test-client-123"
        val customerId = generateCustomerId(clientId)

        assertTrue(
            verifyCustomerId(customerId, clientId),
            "Generated ID should verify correctly with original client"
        )
    }

    @Test
    fun `test decodeCustomerId with correct client`() {
        val clientId = "test-client-123"
        val customerId = generateCustomerId(clientId)

        val decoded = decodeCustomerId(customerId, clientId)
        assertNotNull(decoded, "Should decode successfully")
        assertEquals(26, decoded?.length, "Decoded ULID should be 26 characters")
    }

    @Test
    fun `test isValidCustomerIdFormat with valid IDs`() {
        // Valid 26-character Crockford base32
        val validId = "0123456789ABCDEFGHJKMNPQRS"
        assertTrue(isValidCustomerIdFormat(validId), "Should be valid Crockford base32")

        // Lowercase should also be valid
        assertTrue(isValidCustomerIdFormat(validId.lowercase()), "Lowercase should also be valid")
    }

    @Test
    fun `test isValidCustomerIdFormat rejects invalid formats`() {
        // Too short
        assertFalse(isValidCustomerIdFormat("0123456789ABCDEFGHIJKLMN"), "Should reject: Too short")

        // Too long
        assertFalse(
            isValidCustomerIdFormat("0123456789ABCDEFGHJKMNPQRSTVWXYZ123"),
            "Should reject: Too long"
        )

        // Contains I (not in Crockford base32)
        assertFalse(
            isValidCustomerIdFormat("0I23456789ABCDEFGHJKMNPQRSTVW"),
            "Should reject: Contains I"
        )

        // Contains L (not in Crockford base32)
        assertFalse(
            isValidCustomerIdFormat("01L3456789ABCDEFGHJKMNPQRSTVW"),
            "Should reject: Contains L"
        )

        // Contains O (not in Crockford base32)
        assertFalse(
            isValidCustomerIdFormat("012O456789ABCDEFGHJKMNPQRSTVW"),
            "Should reject: Contains O"
        )

        // Contains U (not in Crockford base32)
        assertFalse(
            isValidCustomerIdFormat("0123456789ABCDEFGHJKMNPQRSTVU"),
            "Should reject: Contains U"
        )
    }

    @Test
    fun `test customerId uniqueness`() {
        val clientId = "consistent-client"
        val ids = (1..100).map { generateCustomerId(clientId) }.toSet()

        assertEquals(100, ids.size, "All generated IDs should be unique")
    }

    @Test
    fun `test all generated IDs verify correctly`() {
        val clientId = "test-client"
        val ids = (1..50).map { generateCustomerId(clientId) }

        for (id in ids) {
            assertTrue(
                verifyCustomerId(id, clientId),
                "Each ID should verify with its generating client"
            )
        }
    }

    @Test
    fun `test generated IDs have valid format`() {
        val clientId = "test"
        val generated = generateCustomerId(clientId)

        assertTrue(
            isValidCustomerIdFormat(generated),
            "Generated ID should be valid format"
        )
    }
}
