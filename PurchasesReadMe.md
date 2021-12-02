
StreamSDK Purchases Module
=====================
The Purchases SDK allows CloudPay users a simple way to purchase Subscriptions and In App Purchases from the Play Store and integrate them into CloudPay.

It depends on the Authentication module to provide an authenticated CloudPay user to tie the associated purchases to.

The module includes the ability to fetch the list of available purchases from the Android Playstore backend and supply it either via a delegate or on demand (if available), as well as completing the purchase via the Play Store and sending the receipt to CloudPay for validation and access to entitlements

The Android BillingClient package is not a required import in the app itself.

## Installing the Purchases Module

Add the jitpack repository to your project level build.gradle

```
allprojects {
       repositories {
           ....
           maven { url "https://jitpack.io" }
       }
  }
```

In your app level build.gradle file, add the dependencies required

```  
    implementation "com.github.StreamAMG.streamamg-sdk-android:streamamg-sdk-core:(version number)"
    implementation "com.github.StreamAMG.streamamg-sdk-android:streamamg-sdk-authentication:(version number)"
    implementation "com.github.StreamAMG.streamamg-sdk-android:streamamg-sdk-purchases:(version number)"
```  

Sync your Gradle files, and the Purchases module should be available for use.

API Overview
============

##Setting up Purchases

The purchase SDK should be accessed via it's singleton instance

```
val iapModule = AMGPurchases.getInstance()
```

##Setting a valid URL

It is required that a valid URL is passed to the SDK before it is usable:

```
iapModule.updatePurchaseURL("https:validURL.test.com/")
```
The URL should be followed by a trailing front slash

##The AMGPurchaseListener

To react to any product lists or purchases made with the Purchases module, a delegate is provided:

``` Kotlin
interface AMGPurchaseListener {
    func purchaseSuccessful(purchase: AMGInAppPurchase)
    func purchaseFailed(purchase: AMGInAppPurchase, error: StreamAMGError)
    func purchasesAvailable(purchases: ArrayList<AMGInAppPurchase>)
}
```

This listener is set using the following method:

``` Kotlin
    iapModule.setListener(listener) //Where 'listener' is a class that conforms to AMGPurchaseListener
```

Creating a BillingClient and fetching packages from the Play Store
========

Although the Android BillingClient package is not directly accessed in the app itself, the Purchases module needs to set one up, this must be instigated from the app before the Play Store can be used, this is achieved by using the following function:

``` Kotlin
    fun createBillingClient(activity: Activity, skuList: ArrayList<String>? = null)
```

To fetch available packages from Play, a list of desired packages is required, these packages can either be passed to the purchases module as a String array in the 'createBillingClient' function, or collected by the SDK from the packages endpoint.

To return the available packages from a list that you provide, use the following call:

``` Kotlin
    iapModule.createBillingClient(activity, arrayListOf("product1", "product2", "product3")) // 'activity' is a valid Android activity
```

This will only return the products specified if they exist in Play Store.

To return any products that are available in CloudPay (via the packages endpoint), then simply call:

``` Kotlin
    iapModule.createBillingClient(activity) // 'activity' is a valid Android activity
```

This calls the packages endpoint, creates the list of required packages and then retrieves all available packages in AppStoreConnect

For both of these calls, the available packages are delivered to the AMGPurchaseDelegate method:

``` Kotlin
    func purchasesAvailable(purchases: ArrayList<AMGInAppPurchase>)
```

which should then update the UI if required.


The AMGInAppPurchase model
=========

To simplify purchases, and to remove the necesity of importing StoreKit into any views which require it, all purchases available to the user are represented by the AMGInAppPurchase model:

``` Kotlin
    val purchaseID: String //The ID of the product
    val purchaseName: String //The name of the product as retrieved from Play Store
    val purchasePriceFormatted: String //The price as formatted by Play Store
    val purchaseDescription: String //A description of the product as retrieved from Play Store
```

An array of these products are available from the Purchases module, once retrieved from AppStoreConnect, by calling the following method:

``` Kotlin
     iapModule.availablePurchases()
```

Making a purchase
=======================

To make a purchase with the purchase module, simply use one of the following calls:

``` Kotlin
     purchase(activity: Activity, purchaseID: String) // Where 'purchaseID' is a valid AMGInAppPurchase.purchaseID value
```

or


``` Kotlin
     purchase(activity: Activity, purchase: AMGInAppPurchase)
```

This will start the purchase process for the user and, if successful will send the receipt to StreamAMG for processing, adding the required entitlements to the user's CloudPay account.

The following AMGPurchaseDelegate method :

``` Kotlin
    func purchaseSuccessful(purchase: AMGInAppPurchase)
```

Will listen for a success (Receipt validated and entitlement added).

A failed purchase or receipt validation issue will result in the following method being triggered:
``` Kotlin
    func purchaseFailed(purchase: AMGInAppPurchase, error: StreamAMGError)
```
   
Where 'error' is a standard StreamAMGError (see 'Core' module)


Change Log:
===========

All notable changes to this project will be documented in this section.

### 1.0.1 - Release

### Beta releases

### 0.4 - Purchases Module added to SDK

### 0.1 -> 0.3 - No Purchases Module
