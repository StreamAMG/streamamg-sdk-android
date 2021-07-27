package com.streamamg.streamamg_sdk_authentication.mappers

import com.streamamg.streamamg_sdk_authentication.api.CurrentCustomerSession
import com.streamamg.streamamg_sdk_authentication.model.StreamAMGUser

fun mapStreamAMGUser(session: CurrentCustomerSession?): StreamAMGUser? {
    if (session == null)
        return null

    return StreamAMGUser(
        id = session.id,
        customerId = session.customerId,
        customerDeleted = session.customerDeleted,
        customerFirstName = session.customerFirstName,
        customerLastName = session.customerLastName,
        customerEmailAddress = session.customerEmailAddress,
        customerSubscriptionCount = session.customerSubscriptionCount,
        customerNonExpiringSubscriptionCount = session.customerNonExpiringSubscriptionCount,
        customerEntitlements = session.customerEntitlements,
        customerFullAccessUntil = session.customerFullAccessUntil,
        customerBillingProfileProvider = session.customerBillingProfileProvider,
        customerBillingProfileReference = session.customerBillingProfileReference,
        customerBillingProfileExpiresAt = session.customerBillingProfileExpiresAt,
        customerBillingProfileCreatedAt = session.customerBillingProfileCreatedAt,
        customerBillingProfileLastFailedAt = session.customerBillingProfileLastFailedAt
    )
}