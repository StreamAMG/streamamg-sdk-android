package com.streamamg.streamamg_sdk_purchases.interfaces

import com.streamamg.streamamg_sdk_purchases.models.AMGInAppPurchase
import com.streamamg.streamapi_core.models.StreamAMGError

interface AMGPurchaseListener {
        fun purchaseSuccessful(purchase: AMGInAppPurchase)
        fun purchaseFailed(purchase: AMGInAppPurchase, error: StreamAMGError)
        fun purchasesAvailable(purchases: ArrayList<AMGInAppPurchase>)
}