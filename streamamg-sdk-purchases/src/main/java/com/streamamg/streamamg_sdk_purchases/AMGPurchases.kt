package com.streamamg.streamamg_sdk_purchases

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.Purchase
import com.google.gson.Gson
import com.streamamg.streamamg_sdk_authentication.AuthenticationSDK
import com.streamamg.streamamg_sdk_purchases.interfaces.AMGPurchaseListener
import com.streamamg.streamamg_sdk_purchases.interfaces.IAPInterface
import com.streamamg.streamamg_sdk_purchases.models.*
import com.streamamg.streamamg_sdk_purchases.network.PurchasesAPI
import com.streamamg.streamamg_sdk_purchases.network.PurchasesCall
import com.streamamg.streamamg_sdk_purchases.services.IAPService
import com.streamamg.streamapi_core.StreamAMGSDK
import com.streamamg.streamapi_core.models.StreamAMGError

class AMGPurchases : PurchasesCall(), IAPInterface {

    private var successCallback: ((PurchasesValidationResponse?, StreamAMGError?) -> Unit)? = null
    private var productsSuccessCallback: ((ArrayList<String>?, StreamAMGError?) -> Unit)? = null
    private var currentResponse: PurchasesValidationResponse? = null
    private var currentRequest: PurchasesValidationRequest? = null
    private var purchaseURL = ""
    val coreSDK: StreamAMGSDK = StreamAMGSDK.getInstance()
    val authenticationSdk = AuthenticationSDK.getInstance()

    private var delegate: AMGPurchaseListener? = null

    private val iapService = IAPService()

    companion object {
        private val sdk: AMGPurchases by lazy { AMGPurchases() }
        public fun getInstance(): AMGPurchases {
            return sdk
        }
    }

    init {
        iapService.setDelegate(this)
        purchasesAPI = StreamAMGSDK.getInstance().retroFit()
            ?.newBuilder()
            ?.build()
            ?.create(PurchasesAPI::class.java)
            ?: throw Exception("Core is not initialised")
    }


    fun updatePurchaseURL(baseURL: String) {
        purchaseURL = baseURL
    }

    fun validate(
        purchase: Purchase,
        token: String,
        isSub: Boolean,
        callBack: ((PurchasesValidationResponse?, StreamAMGError?) -> Unit)?
    ) {
        val receipt = purchase.originalJson
        Gson().fromJson(receipt, PurchaseReceipt::class.java)?.let {
            val iap = iapService.iapAvailable.firstOrNull { iapItem ->
                iapItem.purchaseID == it.productId
            }
            val request = PurchasesValidationRequest(
                receipt = PurchasesReceiptData(
                    it.productId,
                    it.purchaseToken,
                    it.packageName,
                    isSub,
                    payment = PurchasePaymentData(
                        iap?.purchaseCountry ?: "",
                        iap?.purchaseCurrency ?: "",
                        iap?.purchaseAmount ?: ""
                    )
                )
            )
            currentRequest = request
            successCallback = callBack
            validatePurchase(request, "${purchaseURL}iap/verify?apisessionid=$token")
        }
    }

    override fun response(response: PurchasesValidationResponse) {
        currentResponse = response
        Log.d("AMGCall", "SUCCESS! ${response.success}")
        successCallback?.invoke(response, null)
    }

    override fun response(response: StreamAMGError) {
        Log.d("AMGCall", "FAILED! ${response.messages}")
    }

    override fun updateIAPUI() {
        delegate?.purchasesAvailable(iapService.iapAvailable)
    }

    override fun validatePurcahse(purchase: Purchase, isSub: Boolean) {
        Log.d("AMGCall", "Calling validation")
        var purchaseFound = false
        iapService.amgPurchaseFrom(purchase)?.let { iap ->
            purchaseFound = true
            var authenticated = false
            authenticationSdk.lastLoginResponse?.authenticationToken?.let { token ->
                authenticated = true
                validate(purchase, token, isSub) { response, error ->
                    if (error == null) {
                        delegate?.purchaseSuccessful(iap)
                    } else {
                        delegate?.purchaseFailed(iap, error)
                    }
                }
            }
            if (!authenticated) {
                delegate?.purchaseFailed(iap, StreamAMGError(0, "User not authenticated"))
            }
        }
        if (!purchaseFound) {
            delegate?.purchaseFailed(
                AMGInAppPurchase(
                    "",
                    "Your Purchase",
                    "",
                    0.0,
                    "",
                    AMGPurchaseType.subscription,
                    "",
                    "",
                    ""
                ), StreamAMGError(0, "An unknown error has occurred")
            )
        }
    }

    fun setListener(listener: AMGPurchaseListener) {
        delegate = listener
    }

    fun setContext(context: Context) {
        iapService.setContext(context)
    }

    fun createBillingClient(activity: Activity, skuList: ArrayList<String>? = null) {
        if (skuList.isNullOrEmpty()) {
            retrieveSKUs() { skus, amgError ->
                skus?.let {
                    iapService.skuList = it
                    iapService.createBillingClient(activity)
                }
                amgError?.let { amgError ->
                    Log.e(
                        "StreamAMG",
                        "An error occurred retrieving the list of valid SKUs - ${amgError.getMessages()}"
                    )
                }
            }
        } else {
            iapService.skuList = skuList
            iapService.createBillingClient(activity)
        }
    }

    fun purchase(activity: Activity, purchaseID: String) {
        iapService.initiatePurchase(activity, purchaseID)
    }

    fun purchase(activity: Activity, purchase: AMGInAppPurchase) {
        iapService.initiatePurchase(activity, purchase.purchaseID)
    }

    fun availablePurchases(): ArrayList<AMGInAppPurchase> {
        return iapService.iapAvailable
    }

    fun retrieveSKUs(callBack: ((ArrayList<String>?, StreamAMGError?) -> Unit)?) {
        productsSuccessCallback = callBack
        callPackages("${purchaseURL}api/v1/package?type=iap")
    }

    override fun productResponse(response: StreamAMGError) {
        productsSuccessCallback?.invoke(null, response)
    }

    override fun productResponse(response: PurchasePackagesResponse) {
        productsSuccessCallback?.invoke(response.packages(), null)
    }
}