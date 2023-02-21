package com.streamamg.streamamg_sdk_authentication.model

import com.google.gson.annotations.SerializedName
import org.json.JSONObject
import java.util.*

sealed class UserSummary {
    data class Error(val error: Throwable?) : UserSummary()

    data class UserSummaryRequest(val firstName: String, val lastName: String, val CustomFields: Any? = JSONObject.NULL) : UserSummary()
    data class Subscriptions(
        @SerializedName("Id")
        val id: String,
        @SerializedName("Status")
        val status: String,
        @SerializedName("ExpiryDate")
        val expiryDate: Date,
        @SerializedName("IsIAP")
        val isIAP: Boolean,
        @SerializedName("Package")
        val _package: Package,
        @SerializedName("Type")
        val type: String?,
        @SerializedName("CurrencyCode")
        val currencyCode: String?,
        @SerializedName("RenewalDate")
        val renewalDate: Date?
    ) : UserSummary()

    data class Package(
        @SerializedName("Id")
        val id: String,
        @SerializedName("Name")
        val name: String,
        @SerializedName("Title")
        val title: String,
        @SerializedName("Description")
        val packageDescription: String,
        @SerializedName("Type")
        val type: String,
        @SerializedName("Amount")
        val amount: Int,
        @SerializedName("CurrencyCode")
        val currencyCode: String?,
        @SerializedName("Interval")
        val interval: String?,
        @SerializedName("Duration")
        val duration: String?,
        @SerializedName("TrialDuration")
        val trialDuration: String?
    ) : UserSummary()

    data class CustomField(
        @SerializedName("Id")
        val id: String,
        @SerializedName("Label")
        val label: String,
        @SerializedName("Required")
        val customFieldRequired: Boolean,
        @SerializedName("Value")
        val value: String?
    ) : UserSummary()

    data class CardDetails(
        @SerializedName("Provider")
        val provider: String,
        @SerializedName("Reference")
        val reference: String,
        @SerializedName("Country")
        val country: String,
        @SerializedName("Expires")
        val expires: Date
    ) : UserSummary()

    data class BillingDetails(
        @SerializedName("AddressCountry")
        val addressCountry: String,
        @SerializedName("AddressCity")
        val addressCity: String,
        @SerializedName("AddressLine1")
        val addressLine1: String,
        @SerializedName("AddressLine2")
        val addressLine2: String,
        @SerializedName("AddressState")
        val addressState: String,
        @SerializedName("AddressZip")
        val addressZip: String,
        @SerializedName("CardDetails")
        val cardDetails: CardDetails
    ) : UserSummary()

    data class UserSummaryResponse(
        @SerializedName("EmailAddress")
        val emailAddress: String?,
        @SerializedName("FirstName")
        val firstName: String?,
        @SerializedName("LastName")
        val lastName: String?,
        @SerializedName("status")
        val status: String?,
        @SerializedName("error")
        val error: String?,
        @SerializedName("message")
        val message: String?,
        @SerializedName("CustomFields")
        val customFields: List<CustomField>?,
        @SerializedName("BillingDetails")
        val billingDetails: BillingDetails?,
        @SerializedName("Subscriptions")
        val subscriptions: List<Subscriptions>?
    ) : UserSummary()
}
