package com.fivestar.support

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Response type (bug, feature request, etc.)
 */
@Serializable
data class ResponseType(
    val id: String,
    val name: String,
    val slug: String,
    val color: String,
    val icon: String
)

/**
 * Result of generating a customer ID from the server
 */
@Serializable
data class GenerateCustomerIdResult(
    @SerialName("customerId")
    val customerId: String,
    @SerialName("expiresAt")
    val expiresAt: String,
    @SerialName("deviceId")
    val deviceId: String
)

/**
 * Options for submitting a response
 */
@Serializable
data class SubmitResponseOptions(
    @SerialName("customerId")
    val customerId: String,
    val title: String,
    val description: String,
    @SerialName("typeId")
    val typeId: String,
    val email: String? = null,
    val name: String? = null,
    val metadata: JsonObject? = null
)

/**
 * Result of submitting a response
 */
@Serializable
data class SubmitResponseResult(
    val success: Boolean,
    @SerialName("responseId")
    val responseId: String,
    val message: String? = null
)

/**
 * Options for registering a customer
 */
@Serializable
data class RegisterCustomerOptions(
    val email: String? = null,
    val name: String? = null,
    val metadata: JsonObject? = null
)

/**
 * Customer information
 */
@Serializable
data class CustomerInfo(
    val id: String,
    @SerialName("customerId")
    val customerId: String,
    val email: String? = null,
    val name: String? = null
)

/**
 * Result of registering a customer
 */
@Serializable
data class RegisterCustomerResult(
    val success: Boolean,
    val customer: CustomerInfo? = null,
    val message: String? = null
)

/**
 * Customer verification result
 */
@Serializable
data class VerifyCustomerResult(
    val valid: Boolean,
    val message: String? = null
)

/**
 * FiveStar API Error
 */
data class FiveStarAPIError(
    val message: String,
    val statusCode: Int? = null
) : Exception(message)
