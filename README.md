
StreamAMGSDK   [![](https://jitpack.io/v/StreamAMG/streamamg-sdk-android.svg)](https://jitpack.io/#StreamAMG/streamamg-sdk-android)
=========
The StreamAMGSDK provides simple and efficient access to StreamAMG's APIs and services

There are currently four modules available, each of which perform a particular task, or set of tasks, within the SDK, they are:

Core:
  The Core module provides networking, logging and batch processing functionality to the other modules. It is a mandatory requirement of StreamSDK that Core is included in your project
  [Full details](CoreReadMe.md)

CloudMatrix:
  CloudMatrix provides a historical reference of video, audio and other media types for specific events.
  [Full details](CloudMatrixReadMe.md)

StreamPlay:
  StreamPlay contains information regarding upcoming events
  [Full details](StreamPlayReadMe.md)

Authentication:
  Covers logging in, getting a token, KSession, and logging out
  [Full details](AuthenticationReadMe.md)

PlayKit:
  PlayKit provides video playback for Stream AMG clients
  [Full details](PlayKitReadMe.md)

 PlayKit2Go:
   Download and playback media for PlayKit
   [Full details](PlayKit2GoReadMe.md)

Purchases:
   Integrate IAPs into the StreamAMG CloudPay backend
   [Full details](PurchasesReadMe.md)


  To use the modules the following steps should be followed:

## Installation

Add the jitpack repository to your project level build.gradle's allprojects block:

```
allprojects {
       repositories {
           ....
           maven { url "https://jitpack.io" }
       }
  }
```

In your app level build.gradle file, add the dependencies required - you MUST add core for any module other than PlayKit....

  ```  
    implementation "com.github.StreamAMG.streamamg-sdk-android:streamamg-sdk-core:(version number)"
    implementation "com.github.StreamAMG.streamamg-sdk-android:streamamg-sdk-cloudmatrix:(version number)"
    implementation "com.github.StreamAMG.streamamg-sdk-android:streamamg-sdk-streamplay:(version number)"
    implementation "com.github.StreamAMG.streamamg-sdk-android:streamamg-sdk-authentication:(version number)"
    implementation "com.github.StreamAMG.streamamg-sdk-android:streamamg-sdk-playkit:(version number)"
    implementation "com.github.StreamAMG.streamamg-sdk-android:streamamg-sdk-playkit2go:(version number)"
    implementation "com.github.StreamAMG.streamamg-sdk-android:streamamg-sdk-purchases:(version number)"
  ```  

alternatively, to install all SDK elements, use the following line:

```  
  implementation "com.github.StreamAMG:streamamg-sdk-android:(version number)"
```

Sync your Gradle files, and the AMG SDK should import and be available for use.

Change Log:
===========

All notable changes to this project will be documented in this section.

### 1.1.4 - PlayKit and IAP enhancements

### 1.1.3 - Exposed PlayKit property and PlayKit updates

### 1.1.2 - Fixed bitrate and offline issues

### 1.1.1 - PlayKit minor update

### 1.1.0 - Improved bitrate selector UI and Playkit updates

### 1.0.3 - PlayKit HLS casting update

### 1.0.2 - PlayKit minor updates on standard control

### 1.0.1 - PlayKit minor updates

### 1.0 - Release

### Beta releases

### 0.4 - Purchases and PlayKit2Go SDKs added

### 0.3 - Updates to Core and major Playkit feature enhancements

### 0.2 -> 0.2.1 PlayKit bug fixes

### 0.1 - Initial build
