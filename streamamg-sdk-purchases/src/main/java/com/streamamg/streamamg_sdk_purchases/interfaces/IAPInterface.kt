package com.streamamg.streamamg_sdk_purchases.interfaces

import com.android.billingclient.api.Purchase

internal interface IAPInterface {
    fun updateIAPUI()
    fun validatePurcahse(purchase: Purchase, isSub:Boolean)
}