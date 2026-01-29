package com.fivestar.support

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FiveStarClientTest {

    // MARK: - FiveStarClient Tests

    @Test
    fun `test client initialization with config`() {
        val config = FiveStarClientConfig(clientId = "test-client")
        val client = FiveStarClient(config)

        assertEquals("https://fivestar.support/c/test-client", client.getPublicUrl())
    }

    @Test
    fun `test client initialization with parameters`() {
        val client = FiveStarClient(clientId = "test-client")

        assertEquals("https://fivestar.support/c/test-client", client.getPublicUrl())
    }

    @Test
    fun `test client with custom API URL`() {
        val client = FiveStarClient(
            clientId = "test-client",
            apiUrl = "https://custom.example.com"
        )

        assertEquals("https://custom.example.com/c/test-client", client.getPublicUrl())
    }

    @Test
    fun `test getPublicUrl with locale`() {
        val client = FiveStarClient(clientId = "test-client")

        assertEquals("https://fivestar.support/fr/c/test-client", client.getPublicUrl("fr"))
        assertEquals("https://fivestar.support/de/c/test-client", client.getPublicUrl("de"))
    }

    @Test
    fun `test getPublicUrl without locale`() {
        val client = FiveStarClient(clientId = "test-client")

        assertEquals("https://fivestar.support/c/test-client", client.getPublicUrl())
        assertEquals("https://fivestar.support/c/test-client", client.getPublicUrl(null))
    }

    @Test
    fun `test getPublicUrl with empty locale`() {
        val client = FiveStarClient(clientId = "test-client")

        assertEquals("https://fivestar.support/c/test-client", client.getPublicUrl(""))
    }
}
