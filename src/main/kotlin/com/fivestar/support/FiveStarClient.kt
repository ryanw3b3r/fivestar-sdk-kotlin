package com.fivestar.support

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Configuration for the FiveStar Support client
 */
data class FiveStarClientConfig(
    val clientId: String,
    val apiUrl: String = "https://fivestar.support",
    val platform: String? = null,
    val appVersion: String? = null,
    val deviceModel: String? = null,
    val osVersion: String? = null
)

/**
 * FiveStar Support Client
 *
 * Simplified client for interacting with the FiveStar Support API.
 * Customer IDs are now generated server-side for improved security.
 */
class FiveStarClient(private val config: FiveStarClientConfig) {

    private val clientId: String = config.clientId
    private val apiUrl: String = config.apiUrl.trimEnd('/')

    private val httpClient = HttpClient {
        expectSuccess = false // We'll handle errors manually
    }

    private val json = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Initialize with individual parameters
     */
    constructor(
        clientId: String,
        apiUrl: String = "https://fivestar.support",
        platform: String? = null,
        appVersion: String? = null,
        deviceModel: String? = null,
        osVersion: String? = null
    ) : this(
        FiveStarClientConfig(
            clientId = clientId,
            apiUrl = apiUrl,
            platform = platform,
            appVersion = appVersion,
            deviceModel = deviceModel,
            osVersion = osVersion
        )
    )

    // MARK: - Private Helpers

    /**
     * Get the API URL for a given path.
     */
    private fun getUrl(path: String): String {
        return "$apiUrl$path"
    }

    /**
     * Get headers including device information for fingerprinting.
     */
    private fun getHeaders(): Headers {
        return Headers.build {
            append(ContentType, "application/json")

            // Add device fingerprinting headers
            config.platform?.let { append("X-FiveStar-Platform", it) }
            config.appVersion?.let { append("X-FiveStar-App-Version", it) }
            config.deviceModel?.let { append("X-FiveStar-Device-Model", it) }
            config.osVersion?.let { append("X-FiveStar-OS-Version", it) }
        }
    }

    /**
     * Perform a GET request
     */
    private suspend fun <T> get(path: String, asType: Class<T>): T = withContext(Dispatchers.IO) {
        val response: HttpResponse = httpClient.get(getUrl(path)) {
            headers { getHeaders().forEach { k, v -> append(k, v) } }
        }

        if (response.status != HttpStatusCode.OK) {
            throw FiveStarAPIError(
                message = "HTTP ${response.status.value}",
                statusCode = response.status.value
            )
        }

        response.body()
    }

    /**
     * Perform a POST request with a body
     */
    private suspend fun <T : Any> post(path: String, body: Any): T = withContext(Dispatchers.IO) {
        val response: HttpResponse = httpClient.post(getUrl(path)) {
            headers { getHeaders().forEach { k, v -> append(k, v) } }
            setBody(body)
        }

        if (response.status != HttpStatusCode.OK) {
            // Try to parse error message
            try {
                val errorBody = response.body<Map<String, String>>()
                val errorMessage = errorBody["error"] ?: errorBody["message"]
                throw FiveStarAPIError(
                    message = errorMessage ?: "HTTP ${response.status.value}",
                    statusCode = response.status.value
                )
            } catch (e: Exception) {
                throw FiveStarAPIError(
                    message = "HTTP ${response.status.value}",
                    statusCode = response.status.value
                )
            }
        }

        response.body()
    }

    // MARK: - Public API

    /**
     * Get all available response types for this client.
     *
     * @return Array of response types
     */
    suspend fun getResponseTypes(): List<ResponseType> {
        @Serializable
        data class ResponseTypesResponse(val types: List<ResponseType>? = null)

        val result = get<ResponseTypesResponse>(
            path = "/api/responses/types?clientId=$clientId"
        )
        return result.types ?: emptyList()
    }

    /**
     * Generate a new customer ID from the server.
     *
     * Customer IDs are now generated server-side with cryptographic signing.
     * This replaces the previous client-side generation approach.
     *
     * @return Generated customer ID with expiration info
     */
    suspend fun generateCustomerId(): GenerateCustomerIdResult {
        @Serializable
        data class GenerateRequest(val clientId: String)

        return post(
            path = "/api/customers/generate",
            body = GenerateRequest(clientId = clientId)
        )
    }

    /**
     * Register a customer ID for this client.
     *
     * This should be called after generating a customer ID to associate
     * it with optional customer information (email, name).
     *
     * @param customerId The customer ID from generateCustomerId()
     * @param options Optional customer information
     * @return Registration result
     */
    suspend fun registerCustomer(
        customerId: String,
        options: RegisterCustomerOptions? = null
    ): RegisterCustomerResult {
        @Serializable
        data class RegisterRequest(
            val clientId: String,
            @SerialName("customerId")
            val customerId: String,
            val email: String? = null,
            val name: String? = null
        )

        return post(
            path = "/api/customers",
            body = RegisterRequest(
                clientId = clientId,
                customerId = customerId,
                email = options?.email,
                name = options?.name
            )
        )
    }

    /**
     * Check if a customer ID is valid and registered for this client.
     *
     * @param customerId The customer ID to verify
     * @return Verification result
     */
    suspend fun verifyCustomer(customerId: String): VerifyCustomerResult {
        @Serializable
        data class VerifyRequest(
            val clientId: String,
            @SerialName("customerId")
            val customerId: String
        )

        return try {
            post(
                path = "/api/customers/verify",
                body = VerifyRequest(
                    clientId = this.clientId,
                    customerId = customerId
                )
            )
        } catch (e: FiveStarAPIError) {
            VerifyCustomerResult(valid = false, message = "Verification failed")
        }
    }

    /**
     * Submit a new response/feedback on behalf of a customer.
     *
     * @param options Response options including customer ID, title, description, and type
     * @return The submitted response result
     */
    suspend fun submitResponse(options: SubmitResponseOptions): SubmitResponseResult {
        @Serializable
        data class SubmitRequest(
            val clientId: String,
            @SerialName("customerId")
            val customerId: String,
            val title: String,
            val description: String,
            @SerialName("responseTypeId")
            val responseTypeId: String,
            @SerialName("customerEmail")
            val customerEmail: String? = null,
            @SerialName("customerName")
            val customerName: String? = null
        )

        return post(
            path = "/api/responses",
            body = SubmitRequest(
                clientId = clientId,
                customerId = options.customerId,
                title = options.title,
                description = options.description,
                responseTypeId = options.typeId,
                customerEmail = options.email,
                customerName = options.name
            )
        )
    }

    /**
     * Get a public feedback page URL for this client.
     *
     * @param locale Optional locale for the page
     * @return The public URL
     */
    fun getPublicUrl(locale: String? = null): String {
        val localePrefix = if (!locale.isNullOrEmpty()) "/$locale" else ""
        return "$apiUrl$localePrefix/c/$clientId"
    }

    /**
     * Close the HTTP client when done
     */
    fun close() {
        httpClient.close()
    }
}

// Empty object for POST requests without body
private object EmptyRequest
