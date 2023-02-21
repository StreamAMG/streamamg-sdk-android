package com.streamamg.streamamg_sdk_purchases.services

import android.app.Activity
import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import com.google.gson.Gson
import com.streamamg.streamamg_sdk_purchases.AMGPurchaseType
import com.streamamg.streamamg_sdk_purchases.interfaces.IAPInterface
import com.streamamg.streamamg_sdk_purchases.models.AMGInAppPurchase
import com.streamamg.streamamg_sdk_purchases.models.PurchaseReceipt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.util.ArrayList
import java.util.HashMap
import kotlin.coroutines.CoroutineContext

class IAPService: SkuDetailsResponseListener, CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext = job + Dispatchers.IO
    val skusWithSkuDetails = MutableLiveData<Map<String, SkuDetails>>()
    internal var skuAvailable: HashMap<String, SkuDetails> = HashMap()
    internal var iapAvailable: ArrayList<AMGInAppPurchase> = ArrayList()
    private var iapDelegate: IAPInterface? = null
    var currentSKUType = BillingClient.SkuType.SUBS
    var skuList: ArrayList<String> = ArrayList()
    lateinit var billingClient: BillingClient
    var appContext: Context? = null

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
            } else {
                // Handle any other error codes.
            }
        }

    private fun handlePurchase(purchase: Purchase) {
        var isSub = false
        amgPurchaseFrom(purchase)?.let {
            isSub = it.purchaseType == AMGPurchaseType.subscription
        }
        val acknowledgePurchaseResponseListener: AcknowledgePurchaseResponseListener = AcknowledgePurchaseResponseListener {
            if (it.responseCode == 0) {
                iapDelegate?.validatePurchase(purchase, isSub)
            }
        }

        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                billingClient.acknowledgePurchase(acknowledgePurchaseParams.build(), acknowledgePurchaseResponseListener)
            } else {
                iapDelegate?.validatePurchase(purchase, isSub)
            }
        }
    }

    fun createBillingClient(activity: Activity) {
        billingClient = BillingClient.newBuilder(activity)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        if (!billingClient.isReady) {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

                        iapAvailable.clear()
                        querySkuDetails()
                    }
                }

                override fun onBillingServiceDisconnected() {
                }
            })
        }

    }


    fun querySkuDetails() {
        val skuDetailsParams = SkuDetailsParams.newBuilder()
        skuDetailsParams.setSkusList(skuList).setType(currentSKUType)
        billingClient.querySkuDetailsAsync(skuDetailsParams.build(), this)
    }

    private fun includePurchase(purchases: HashMap<String, SkuDetails>){
        purchases.forEach { type ->
            skuAvailable[type.key] = type.value
            val formattedPrice = type.value.price.replace("[^0123456789.,]".toRegex(), "")
            val tm = appContext?.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            val country = tm?.simCountryIso?.uppercase() ?: ""
            val purch = AMGInAppPurchase(type.key, type.value.description, type.value.price, 0.0, type.value.description, if (currentSKUType == BillingClient.SkuType.SUBS) AMGPurchaseType.subscription else AMGPurchaseType.nonconsumable, country, type.value.priceCurrencyCode, formattedPrice)
            iapAvailable.add(purch)
        }
    }

    override fun onSkuDetailsResponse(billingResult: BillingResult, skuDetailsList: MutableList<SkuDetails>?) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (skuDetailsList == null) {
                    skusWithSkuDetails.postValue(emptyMap())
                    iapDelegate?.updateIAPUI()
                } else
                    skusWithSkuDetails.postValue(HashMap<String, SkuDetails>().apply {
                        for (details in skuDetailsList) {
                            put(details.sku, details)
                        }
                    }.also { postedValue ->
                        if (currentSKUType == BillingClient.SkuType.SUBS) {
                                includePurchase(postedValue)
                            currentSKUType = BillingClient.SkuType.INAPP
                            querySkuDetails()
                        } else {
                            includePurchase(postedValue)
                        }
                        iapDelegate?.updateIAPUI()
                    })
            }
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
            BillingClient.BillingResponseCode.DEVELOPER_ERROR,
            BillingClient.BillingResponseCode.ERROR -> {
                Log.e("PTOLog", "onSkuDetailsResponse: $responseCode $debugMessage")
            }
            BillingClient.BillingResponseCode.USER_CANCELED,
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED,
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> {
                // These response codes are not expected.
                Log.wtf("PTOLog", "onSkuDetailsResponse: $responseCode $debugMessage")
            }
        }
    }

    internal fun setDelegate(delegate: IAPInterface?) {
        iapDelegate = delegate
    }

    internal fun setContext(context: Context) {
        appContext = context
    }

    fun initiatePurchase(activity: Activity, sku: String) {
        skuAvailable[sku]?.let {
            val flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(it)
                .build()
            billingClient.launchBillingFlow(activity, flowParams).responseCode
        }
    }

    fun purchaseWithID(id: String) : AMGInAppPurchase? {
        return iapAvailable.find { it.purchaseID == id }
    }

    fun amgPurchaseFrom(purchase: Purchase): AMGInAppPurchase?{
        val receipt = purchase.originalJson
        Gson().fromJson(receipt, PurchaseReceipt::class.java)?.let{ rec ->
            return purchaseWithID(rec.productId)
        }
        return null
    }
}