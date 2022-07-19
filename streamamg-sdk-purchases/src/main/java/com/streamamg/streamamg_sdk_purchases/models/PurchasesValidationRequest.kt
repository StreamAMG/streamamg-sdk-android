package com.streamamg.streamamg_sdk_purchases.models

data class PurchasesValidationRequest(
    val platform: String = "android",
    var receipt: PurchasesReceiptData
)

data class PurchasesReceiptData(
    var productId: String,
    var purchaseIdentifier: String,
    var packageName: String,
    var autoRenewing: Boolean,
    var payment: PurchasePaymentData? = null
)

data class PurchasePaymentData(
    var countryCode: String,
    var currencyCode: String,
    var amount: String
)