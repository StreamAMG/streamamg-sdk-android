
StreamSDK PlayKit2Go Module
=====================
The PlayKit2Go SDK allows Downloading and playback of videos in PlayKit

It depends on the PlayKit module.

## Installing the PlayKit2Go Module

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
    implementation "com.github.StreamAMG.streamamg-sdk-android:streamamg-sdk-playkit:(version number)"
    implementation "com.github.StreamAMG.streamamg-sdk-android:streamamg-sdk-playkit2go:(version number)"
```  

Sync your Gradle files, and the PlayKit2Go module should be available for use.

API Overview
============

##Setting up PlayKit2Go

The PlayKit2Go SDK should be accessed via it's singleton instance

```
val playKit2Go = PlayKit2Go.getInstance()
```

PlayKit2Go does not require an instance of PlayKit to be active (except for during playback), and can be set up at any point in the app's lifecycle

PlayKit2Go manages an internal database, which app developers do not need to access at all. To access this database, however, PlayKit2Go must run a setup function before any attempt is made to use it further.

``` Kotlin
    playKit2Go.setup(context) // 'context' must be a valid non-optional Android Context
```

This setup function not only allows access for PlayKit2Go to the database, but also restarts any downloads that are not complete or have not yet started

##The PlayKit2GoListener

To react to any updates or errors from PlayKit2Go, a listener is provided:

``` Kotlin
interface PlayKit2GoListener {
    fun downloadDidError(item: PlayKitDownloadItem)
    fun downloadDidUpdate(item: PlayKitDownloadItem)
    fun downloadDidComplete(item: PlayKitDownloadItem)
    fun downloadDidChangeStatus(item: PlayKitDownloadItem)
}
```

This listener is set using the following method:

``` Kotlin
    playKit2Go.setListener(listener) //Where 'listener' is a class that conforms to PlayKit2GoListener
```


Removing PlayKit2Go
========
When PlayKit2Go is no longer required, it should be destroyed

``` Kotlin
    playKit2Go.destroy()
```

This removes all callbacks and pauses active downloads


The PlayKitDownloads model
=========

PlayKit2Go keeps track of the status of all requestd downloads on the device and provides a sorted model of them that is available to the app developers on request

The PlayKitDownloads model keeps ArrayLists of all available states of downloads is this structure:

``` Kotlin
    var completed: ArrayList<PlayKitDownloadItem> = ArrayList(),
    var new: ArrayList<PlayKitDownloadItem> = ArrayList(),
    var paused: ArrayList<PlayKitDownloadItem> = ArrayList(),
    var downloading: ArrayList<PlayKitDownloadItem> = ArrayList(),
    var failed: ArrayList<PlayKitDownloadItem> = ArrayList(),
    var metadataLoaded: ArrayList<PlayKitDownloadItem> = ArrayList(),
    var removed: ArrayList<PlayKitDownloadItem> = ArrayList()
```

The PlayKitDownloadItem model is a summary of everything that PlayKit2Go stores in it's database and gives an exact picture of a single download at a specific point in time:

``` Kotlin
    var entryID: String = "",
    var completedFraction: Float = 0.0f, // As a percentage of the total
    var totalSize: Long = 0, // in Bytes
    var currentDownloadedSize: Long = 0, // in Bytes
    var available: Boolean = false,
    var error: PlayKit2GoError? = null
```

The PlayKit2GoError enum returns only if an error is encountered during download:

``` Kotlin
enum class PlayKit2GoError {
    Already_Queued_Or_Completed, Download_Error, Unknown_Error, Download_Does_Not_Exist, Item_Not_Found, Internal_Error
    }
```

Checking the status of downloading media
=======================

To obtain the latest version of the PlayKitDownloads model, the followin function is provided:

``` Kotlin
    playKit2Go.fetchAllStoredItems()
```

This will contain all current information for all downloaded and requested media

The model can also be queried for a particular entryID's download 'percentage'

``` Kotlin
    playKitDownloadModel.percentageForItem(ENTRYID)
```
This will return either the actual percentage of download completed for downloads in progress, 0 if the download has not yet started or 100 if the download is complete.
Failed downloads or items that have not been queued for download will return -1

Downloading media
=======================

To download media, you must pass all relevent information to PlayKit2Go:

``` Kotlin
     fun download(serverUrl: String, partnerID: Int, entryID: String, ks: String? = null)
```


This will start the download process for the media, and will report back to the listener for each individual download, allowing the developer to keep the UI up to date.


Local media playback
=======================

If the item is available for playback (PlayKitDownloadItem.available == true) then the media can be played through PlayKit by simply sending it's entryID:

``` Kotlin
    playKit.loadPlayKit2GoMedia(ENTRY_ID) // where playKit is a valid instance of tthe PlayKit module and ENTRY_ID is the ID of some downloaded media
```

Removing media
=======================

To remove downloaded media, you should call the following function:

``` Kotlin
     fun remove(entryID: String)
```

This will immediately remove the media from local storage and also from the database.

Media can be 'removed' at any point in it's download lifecycle, and should be removed before attempting to redownload.


Change Log:
===========

All notable changes to this project will be documented in this section.

### 0.4 - PlayKit2Go Module added to SDK

### 0.1 -> 0.3 - No Purchases Module
