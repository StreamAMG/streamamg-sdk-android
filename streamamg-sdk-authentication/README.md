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

If you are using Gradle to get libraries into your build, you will need to:

Step 1. Add the StreamAMG SDK maven repository to the list of repositories in Project build.gradle:

```
allprojects {
    repositories {
        ...
        maven {
            url "https://api.bitbucket.org/2.0/repositories/sukdev/streamamg-android-sdk/src/releases"
            credentials {
                username USERNAME
                password PASSWORD
            }
            authentication {
                basic(BasicAuthentication)
            }
        }
    }
}
```
Where USERNAME is your BitBucket username and PASSWORD is your app password. Take care not to check these credentials into version control by using a `local.properties` file or similar

Step 2. Add the dependency information in Module app build.gradle:

```
implementation 'com.streamamg:streamamg-sdk-authentication:$VERSION_NAME'
```
Please use the same version for all modules to prevent dependency errors.

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



Change Log:
===========

All notable changes to this project will be documented in this section.

### 0.0.3 - Initial version
### 0.0.4 - Added loginSilent
### 0.0.5 - Replaced callbacks with lambdas