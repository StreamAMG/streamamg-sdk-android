package com.streamamg.streamamg_sdk_authentication.api

import com.google.gson.annotations.SerializedName
import java.util.*

data class CurrentCustomerSession(
    @SerializedName("Id")
    var id: String? = null,

    @SerializedName("CustomerId")
    var customerId: String? = null,

    @SerializedName("CustomerDeleted")
    var customerDeleted: Boolean? = null,

    @SerializedName("CustomerFirstName")
    var customerFirstName: String? = null,

    @SerializedName("CustomerLastName")
    var customerLastName: String? = null,

    @SerializedName("CustomerEmailAddress")
    var customerEmailAddress: String? = null,

    @SerializedName("CustomerSubscriptionCount")
    var customerSubscriptionCount: Int? = null,

    @SerializedName("CustomerNonExpiringSubscriptionCount")
    var customerNonExpiringSubscriptionCount: Int? = null,

    @SerializedName("CustomerEntitlements")
    var customerEntitlements: String? = null,
    @SerializedName("CustomerFullAccessUntil")
    var customerFullAccessUntil: Date? = null,

    @SerializedName("CustomerBillingProfileProvider")
    var customerBillingProfileProvider: String? = null,

    @SerializedName("CustomerBillingProfileReference")
    var customerBillingProfileReference: String? = null,

    @SerializedName("CustomerBillingProfileExpiresAt")
    var customerBillingProfileExpiresAt: Date? = null,

    @SerializedName("CustomerBillingProfileCreatedAt")
    var customerBillingProfileCreatedAt: Date? = null,

    @SerializedName("CustomerBillingProfileLastFailedAt")
    var customerBillingProfileLastFailedAt: Date? = null
)