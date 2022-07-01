package com.streamamg.streamamg_sdk_purchases.models

import com.streamamg.streamamg_sdk_purchases.AMGPurchaseType

data class AMGInAppPurchase(
    val purchaseID: String,
    val purchaseName: String,
    val purchasePriceFormatted: String,
    val purchasePrice: Double,
    val purchaseDescription: String,
    val purchaseType: AMGPurchaseType,
    val purchaseCountry: String,
    val purchaseCurrency: String,
    val purchaseAmount: String
)