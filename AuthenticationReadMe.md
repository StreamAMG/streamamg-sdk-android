StreamSDK Authentication SDK
============================

The StreamSDK Authentication SDK is a light wrapper around the authentication API and provides:

* Logging in via email and password
* Getting a token
* Getting a KSession
* Storing credentials and logging in silently
* Logging out

Quick Start Guide
======

Have a look at `LoginActivity` in `streamamg-sdk-demo` app in this repo for sample usage

Gradle implementation
=====

Add the jitpack repository to your project level build.gradle

```
allprojects {
       repositories {
           ....
           maven { url "https://jitpack.io" }
       }
  }
```

In your app level build.gradle file, add the dependencies required - you MUST add core for Authentication

```  
    implementation "com.github.StreamAMG.streamamg-sdk-android:streamamg-sdk-core:(version number)"
    implementation "com.github.StreamAMG.streamamg-sdk-android:streamamg-sdk-authentication:(version number)"
```  

API Overview
============

## Initialisation

The SDK instance is available as a singleton and must be initialised before use:

```
val authenticationSdk = AuthenticationSDK.getInstance()
authenticationSdk.initWithURL("https://my.client.url.payments/", "lang=" + Locale.getDefault().language)
```

`initWithURL` takes 2 parameters: the base url of the service, and a second optional parameter string which is appended to all requests

## Usage

Once initialised, call the functions of the SDK with callbacks:
```
authenticationSdk.login(email, password) { result ->
    when (result) {
        is LoginResult.LoginOK -> onloginSuccess()
        else -> handleLoginError(result)
    }
}
```

Using custom SSO
=======================
Authentication SDK supports custom SSO implementation that are correctly configured with CloudPay and are able to generate a valid use JWT token.

## Start the session with custom SSO

In order to comunicate with CloudPay the app needs to start the session with the users token.

```
authenticationSdk.startSession("jwtToken",
    onSuccess = {

    },
    onError = {
        error ->
    }
)
```
## Update user summary
The update user summary function allows a user to update their user details if the user is already logged in via StreamAMGSDK. Please be aware that the API cannot edit the user's email.

This function needs to be utilised after a new user is registered and every time the user wants to update the metadata.

```
authenticationSdk.updateUserSummary(firstname,lastname,
    onSuccess = {
        userSummary ->
    },
    onError = {
        error ->
    }
)
```

## Update user summary with token
The update user summary function allows a user to update their user details if user has a valid JWT token. Please be aware that the API cannot edit the user's email.

This function needs to be utilised after a new user is registered and every time the user wants to update the metadata.

```
authenticationSdk.updateUserSummary(firstname,lastname, token
    onSuccess = {
        userSummary ->
    },
    onError = {
        error ->
    }
)
```
## Retrieve user summary
The retrieve user summary function allows a user to retrieve their user details if the user has a valid token.

This function can be used to retrieve the user account details such as ProfileInformation, BillingDetails, CardDetails,Subscription Details.

```
authenticationSdk.getUserSummary(token
    onSuccess = {
        userSummary ->
    },
    onError = {
        error ->
    }
)
```
Change Log:
===========

All notable changes to this project will be documented in this section.

### 1.0.1 - Release

### Beta releases

### 0.2 -> 0.4 - No changes to Authentication

### 0.1 - Initial build
