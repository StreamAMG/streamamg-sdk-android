
StreamSDK Core Module
=====================
StreamSDK-Core is the only mandatory component in the StreamSDK toolkit. It a requirement for any of the other StreamSDK modules to have StreamSDK-Core implemented before they will function.

Core provides the networking and logging components of the SDK as well as Error reporting and common model and constants components.

The version of Core implemented in a project should always be the same as, or newer than the component modules it is supporting.

Quick Start Guide
======

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
implementation 'com.streamamg:streamamg-sdk-core:$VERSION_NAME'
```

Please use the same version for all 3 modules to prevent dependency errors.

API Overview
============

##Initialisation

Once Core has been added to the project, it must be initialised before any components can be accessed.

fun initialise(context: Context, okHttpClient: OkHttpClient? = null, gsonImplementation: Gson? = null, env: StreamAPIEnvironment = StreamAPIEnvironment.PRODUCTION)

A standard initialisation of Core, which includes the default OKHTTP and GSON implementations would be:

**Java:**
```java
StreamAMGSDK.INSTANCE.initialise(this, null, null, StreamAPIEnvironment.STAGING);
```
**Kotlin:**
```kotlin
StreamAMGSDK.initialise(this, env = StreamAPIEnvironment.STAGING)
```
This can be configured with custom OKHTTP or GSON implementations by passing the objects in the function call:

**Java:**
```java
OKHTTP okHTTPInstance = new OKHTTP()
GSON gson = new GSON()
(customise okHTTPInstance and / or gson Instance here)
StreamAMGSDK.INSTANCE.initialise(context,okHTTPInstance,gson, StreamAPIEnvironment.STAGING);
```
**Kotlin:**
```kotlin
val okHTTPInstance = OKHTTP()
val gson = GSON()
(customise okHTTPInstance and / or gson Instance here)
StreamAMGSDK.initialise(context, okHTTPInstance, gson, StreamAPIEnvironment.STAGING)
```
For Kotlin, the environment will default to PRODUCTION if no environment is specified:
StreamAMGSDK.initialise(context)

The environment parameter defines which url the library will use if both production and staging URLs have been provided.


##StreamSDK Internal Logging

Core provides internal logging of all calls and model construction that occurs when modules are used. Logging is disabled by default, but can be enabled either fully, or at a component level, by a call to StreamAMGSDK

To enable full logging:
**Java:**
```java
StreamAMGSDK.INSTANCE.enableLogging();
```
**Kotlin:**
```kotlin
StreamAMGSDK.enableLogging()
```
Logging of the following components can be enabled or disabled:

Network – Details of network calls to servers, including URLs called

Responses – Broken down listings of potentially large text dumps, generally API responses

ModelLogs – Lists of items parsed into modules

BoolValues – (Unused) Logging of Boolean values and descriptors

Standard – Any other feedback, eg: initialisation

These can be activated / deactivated by a call to StreamAMGSDK:

The following calls will enable only Network and Standard logs (Called instead of enableLogging())
**Java:**
```java
StreamAMGSDK.INSTANCE.enableLogging(StreamSDKLogType.Network, StreamSDKLogType.Standard);
```
**Kotlin:**
```kotlin
StreamAMGSDK.enableLogging(StreamSDKLogType.Network, StreamSDKLogType.Standard)
```
You can, similarly, enable all logging and disable, for example only ModelLogs:
**Java:**
```java
StreamAMGSDK.INSTANCE.enableLogging();
StreamAMGSDK.INSTANCE.disable(StreamSDKLogType.ModelLogs);
```
**Kotlin:**
```kotlin
StreamAMGSDK.enableLogging()
StreamAMGSDK.disable(StreamSDKLogType.ModelLogs)
```

##API Error Model
The Core SDK contains a standard error response model that is returned if an unsuccessful request is returned. If a callback error model is not null, it can be assumed the API call did not succeed:
**Kotlin:**
```kotlin
{ response, error ->
    if (error == null) {
        // Process the valid response object (CloudMatrixResponse / StreamPlayResponse) here
    } else {
        // Process the valid error object (StreamAMGError) here
    }
}
```
The model is very simple. It contains the HTTP code returned and an array of String values that may have been passed to the SDK when the error occurred.

These details can be retrieved from the error model using the following calls:

Http code
error.code

Any messages – returned as an array of Strings
error.messages

Any messages – returned as a single String containing all errors
error.getMessages()

##Batch processing

In a normal call to the server, you will likely want the response to be immediately delivered back to the callback so the app can respond accordingly. There may be occasions, however, where it would be preferable for multiple jobs to complete before processing.

In these situations, the StreamSDKBatchJob service can collate any number of jobs, from either a single module or a mixture of any modules, make a request from the API and hold the responses until all jobs have been completed.

Once all jobs are complete, the service will then fire all callbacks.
**Kotlin:**
```kotlin
val queue = StreamSDKBatchJob()
        queue.add(CloudMatrixJob(cmRequest, cmBatchClosure))
        queue.add(StreamPlayJob(spRequest, spBatchClosure))
        ....
        queue.fireBatch()
```

The batch job can be fired as many times as required once created, but will not allow a restart until any running batch has completed

Change Log:
===========

All notable changes to this project will be documented in this section.

### 0.1 - Initial build
### 0.0.2
- Change versioning
- Add ability to publish aars to BitBucket via wagon-git