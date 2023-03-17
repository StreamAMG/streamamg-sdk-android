package com.streamamg.streamamg_sdk_purchases.interfaces

import com.android.billingclient.api.Purchase
import com.streamamg.streamamg_sdk_purchases.models.AMGInAppPurchase
import com.streamamg.streamapi_core.models.StreamAMGError

interface AMGPurchaseListener {
        /**
         * Successfully completed the purchase and validation
         */
        fun purchaseSuccessful(purchase: AMGInAppPurchase)

        /**
         *  Purchase failed with error
         */
        fun purchaseFailed(purchase: AMGInAppPurchase, error: StreamAMGError)

        /**
         * Available iap products for purchase
         */
        fun purchasesAvailable(purchases: ArrayList<AMGInAppPurchase>)

        /**
         *  Successfully completed the purchase, but the validate purchase call failed.
         *  In this case you have to manually call the validatePurchase API
         */
        fun purchaseSuccessfulWithoutValidation(purchase: Purchase, error: StreamAMGError)
}