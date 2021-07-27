package com.streamamg.streamamg_sdk_authentication.model

import java.util.*

data class StreamAMGUser(
    val id: String? = null,
    val customerId: String? = null,
    val customerDeleted: Boolean? = null,
    val customerFirstName: String? = null,
    val customerLastName: String? = null,
    val customerEmailAddress: String? = null,
    val customerSubscriptionCount: Int? = null,
    val customerNonExpiringSubscriptionCount: Int? = null,
    val customerEntitlements: String? = null,
    val customerFullAccessUntil: Date? = null,
    val customerBillingProfileProvider: String? = null,
    val customerBillingProfileReference: String? = null,
    val customerBillingProfileExpiresAt: Date? = null,
    val customerBillingProfileCreatedAt: Date? = null,
    val customerBillingProfileLastFailedAt: Date? = null
)