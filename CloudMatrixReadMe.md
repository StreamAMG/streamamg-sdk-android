
StreamSDK CloudMatrix Module
=====================
The CloudMatrix API, and in extension, the CloudMatrix SDK provides a feed for on demand video clips and other media. The CloudMatrix API is extensive and, potentially, complex for in depth searches, the CloudMatrix SDK aims to reduce complexity whilst handling API returns for all types of calls to the API with a single model.

In general, there are 2 main ways of accessing the API, and an extra 2 endpoints for more specific requests, the SDK handles all 4 request types.

The request types are:

FEED – bringing a static (pre-defined) set of data back, generally a stock list of videos or news feeds, with little to no customisation

SEARCH – searching the entire repository of videos and news feeds for specific items and returning only those

TERMS – checking how often a word or phrase is used in a given string array (Tags, for example)

ENTITLEMENTS – Right now, I just don’t know……

The SDK handles the returning API data in a single model, as well as offering methods for automatically handling paging data.

Quick Start Guide
======

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

In your app level build.gradle file, add the dependencies required - you MUST add core for CloudMatrix

```  
    implementation "com.github.StreamAMG.streamamg-sdk-android:streamamg-sdk-core:(version number)"
    implementation "com.github.StreamAMG.streamamg-sdk-android:streamamg-sdk-cloudmatrix:(version number)"
```  

API Overview
============

##Setting up CloudMatrix
To use the StreamSDK-CloudMatrix module, StreamSDK-Core MUST be initialised.

You should instantiate CloudMatrix to use it. To access a predefined static URL, you can simply instantiate the class without parameters:

var cloudMatrix = CloudMatrix()

To use the SDK’s dynamic feeds or search capabilities, however, you must initialise the module via instantiation.

class CloudMatrix (userID: String, key: String, url: String, debugURL: String? = null, version: String = "v1", language: String = "en")

The bare minimum requirements for initialisation, are a CloudMatrix URL, a UserID and a KeyID

var cloudMatrix = CloudMatrix("(Valid User Name)", "(Valid Key ID)", "https://example.streamamg.com/api")

But, if required, all details can be entered

var cloudMatrix = CloudMatrix("(Valid User Name)", "(Valid Key ID)", "https://example.streamamg.com/api", "https://staging.streamamg.com/api", "V2", "fr")

If a staging URL is not provided, the production URL will be used in staging.

##Making API Requests
The module has two ways of programmatically making requests to the API, as a standard Object, instantiating the request and adding to it through standard dot notation, or as an Object Builder.

Using the Object Builder is the preferred method, but both will work, and are documented below

Once a request has been made and delivered, the SDK will return a valid response model (or an error object) to a callback specified. The responses and errors are discussed later in this document.

There are, generally, 2 parts to sending requests, creating the request itself, and sending the request through the StreamSDK-Core networking system. Occasionally this can be reduced to a single call, but with a little ‘heavy lifting’ beforehand. Where this is possible, it will be noted in the documentation.

In each instance there is an optional callback which allows the user to process any returned data. Although the callback is, technically, optional, it is not seen that there are many advantages of not providing one.
**Java:**
```java
CloudMatrixRequest request = new CloudMatrixRequest() //(See below)

cloudMatrix.callAPI(request, (cloudMatrixResponse, streamAMGError) -> {

             if (streamAMGError == null) {
// Process the valid response object (CloudMatrixResponse) here
             } else {
// Process the valid error object (StreamAMGError) here
}
     return null;
         });
```
**Kotlin:**
```kotlin
val request: CloudMatrixRequest = CloudMatrixRequest //(See below)

cloudMatrix.callAPI(request: request, callBack: callback){ cloudMatrixResponse, streamAMGError ->
    if (streamAMGError == null) {
        // Process the valid cloudMatrixResponse object (CloudMatrixResponse) here
    } else {
        // Process the valid streamAMGError object (StreamAMGError) here
    }
}
```
##Accessing Static Feeds
Using pre-defined complete URLs
Many requests made to the APIs are simple injections of pre-defined URLs that are made available either whilst in development, or at runtime through access to a separate API feed (a config, for example)

These URLs come fully formed and can be sent to CloudMatrix without any intervention from this SDK, there are, however, several advantages to using the SDK to process these requests:

-	Networking is handled
-	Errors are handled
-	A standard model is used for the response
-	Paging, if necessary, is handled

To utilise the CloudMatrix module for this, is as simple as making a basic request.

Using the Builder:
**Kotlin:**
```kotlin
val feed = CloudMatrixRequest
        .FeedBuilder()
        .url("(Fully formed API request URL)")
        .build()
```
Using standard initialisation:
**Kotlin:**
```kotlin
val feed = CloudMatrixRequest(CloudMatrixFunction.FEED, null, ArrayList(), "(Fully formed API request URL)")
```
or:
**Kotlin:**
```kotlin
val feed = CloudMatrixRequest(url = "(Fully formed API request URL)")
```

and then sending it to CloudMatrix using the ‘callAPI’ method:
**Kotlin:**
```kotlin
cloudMatrix.callAPI(feed, callback)
```
Or:
**Kotlin:**
```kotlin
cloudMatrix.callAPI(feed) { response, error ->
    if (error == null) {
        // Process the valid response object (CloudMatrixResponse) here
    } else {
        // Process the valid error object (StreamAMGError) here
    }
}
```
##Using a specific event
If you have only the event details, and wish to make a call to the API using that, it is just as simple.

Please note, the CloudMatrix module must have been initialised before this call is made

Using the Builder:
**Kotlin:**
```kotlin
val feed = CloudMatrixRequest
        .FeedBuilder()
        .event("(Valid event ID")
        .build()
```
or:
**Kotlin:**
```kotlin
val feed = CloudMatrixRequest
        .FeedBuilder("Valid event ID")
        .build()
```

Using standard initialisation:
**Kotlin:**
```kotlin
val feed = CloudMatrixRequest(CloudMatrixFunction.FEED, "(Valid event ID)", ArrayList(), null, 0)
```
or:
**Kotlin:**
```kotlin
val feed = CloudMatrixRequest(event = "(Valid event ID)")
```

and then sending it to CloudMatrix using the ‘callAPI’ method:
**Kotlin:**
```kotlin
CloudMatrix.callAPI(feed, callback)
```
Or:
**Kotlin:**
```kotlin
CloudMatrix.callAPI(feed) { response, error ->
    if (error == null) {
        // Process the valid response object (CloudMatrixResponse) here
    } else {
        // Process the valid error object (StreamAMGError) here
    }
}
```

##Using the StreamSDK-CloudMatrix Search Capabilities
There is a vast array of search and filter capabilities available in CloudMatrix, and compex searches can be constructed in the SDK using the Builder.
**Kotlin:**
```kotlin
val search = CloudMatrixRequest
        .SearchBuilder()
        .isEqualTo(CloudMatrixQueryType.TITLETEXT, "Football")
        .isLessThan(CloudMatrixQueryType.VIDEODURATION, 120)
        .contains(“homeTeam”, “West Ham”)
        .build()
```

There is no limit to the amount of parameters that can be included, or to the types of searches that can be mixed.

Currently there is only ‘AND’ searches, but ‘OR’ is being worked on.


The build components are flexible:
**Kotlin:**
```kotlin
isEqualTo(target: CloudMatrixQueryType, query: String)

isEqualTo(target: String, query: String)

isEqualTo(target: CloudMatrixQueryType, query: Number)

isEqualTo(target: String, query: Number)
```
The ‘target’ is the field in the database being referenced, the ‘query’ is the item being searched for

For all of the following search types, a target can be either one of a pre-defined number of Query Types, or can be a String value, where a query is required, this can be either a String or any Number type. For simplicity, only (target: CloudMatrixQueryType, query: String) examples are shown:

Exact match of word or numbers
**Kotlin:**
```kotlin
.isEqualTo(target: CloudMatrixQueryType, query: String)
```

Value is greater than (or equal to) the query. This can be passed as a String or Number
**Kotlin:**
```kotlin
.isGreaterThan(target: CloudMatrixQueryType, query: String)
.isGreaterThanOrEqualTo(target: CloudMatrixQueryType, query: String)
```

Value is less than (or equal to) the query. This can be passed as a String or Number
**Kotlin:**
```kotlin
.isLessThan(target: CloudMatrixQueryType, query: String)
.isLessThanOrEqualTo(target: CloudMatrixQueryType, query: String)
```

Fuzzy search – “foot” will match ‘right-footed’, ‘football’ and ‘foot’, etc
**Kotlin:**
```kotlin
.isLike(target: CloudMatrixQueryType, query: String)
```

Starting character search – “foot” will match ‘football’ and ‘foot’, but not ‘right-footed’
**Kotlin:**
```kotlin
.startsWith(target: CloudMatrixQueryType, query: String)
```

String array contains specified item
**Kotlin:**
```kotlin
.contains(target: CloudMatrixQueryType, query: String)
```

The following searches require no ‘query’

Return only records that have a specified field
**Kotlin:**
```kotlin
.exists(target: CloudMatrixQueryType)
```

Boolean searches on a field
**Kotlin:**
```kotlin
.isTrue(target: CloudMatrixQueryType)
.isFalse(target: CloudMatrixQueryType)
```

##Batch processing

In a normal call to the server, you will likely want the response to be immediately delivered back to the callback so the app can respond accordingly. There may be occasions, however, where it would be preferable for multiple jobs to complete before processing.

In these situations, the StreamSDKBatchJob service can collate any number of jobs, from either a single module or a mixture of any modules, make a request from the API and hold the responses until all jobs have been completed.

Once all jobs are complete, the service will then fire all callbacks.
**Kotlin:**
```kotlin
        val search1 = CloudMatrixRequest
                .FeedBuilder()
                .url(staticURL1)
                .build()

        val search2 = CloudMatrixRequest
                .FeedBuilder()
                .url(staticurl2)
                .build()

val queue = StreamSDKBatchJob()
        queue.add(CloudMatrixJob(search1, cmBatchClosure))
        queue.add(CloudMatrixJob(search2, cmBatchClosure))
        ....
        queue.fireBatch()
```

The batch job can be fired as many times as required once created, but will not allow a restart until any running batch has completed

##The CloudMatrix Response Model

Accessing retrieved data
When a successful call to the API has been returned, the sdk makes available a data model of type ‘CloudMatrixResponse?’. This model contains the following information:

CloudMatrixResponse  root
-	metadata: CloudMatrixFeedMetaDataModel – All responses
    o	id: String?
    o	name: String?
    o	itle: String?
    o	description: String?
    o	target: String?
-	sections: ArrayList<CloudMatrixSectionModel>? – Feed responses only
    o	id: String?,
    o	name: String?,
    o	itemData: ArrayList<CloudMatrixItemDataModel>? (see ItemDataModel below)
    o	pagingData: CloudMatrixPagingDataModel (see PagingDataModel below)
-	itemData: ArrayList<CloudMatrixItemDataModel>? – Search responses only (see ItemDataModel below)
-	pagingData: CloudMatrixPagingDataModel? – Search responses only (see ItemDataModel below)

CloudMatrixResponse  PagingDataModel
Any response is guaranteed to have paging data included, with direct URLs and Feed responses, this data is contained in the ‘sections’ array, for search responses, this is contained in the root of the response.
The SDK can automatically provide paging, but if it is preferred that it should be handled manually, the correct paging data can be retrieved from the root of CloudMatrixResponse by calling the ‘fetchPagingData(section:Int?)’

val pagingData = response.fetchPagingData() (selects the ‘current’ section if Feed response, or all data if a search

val pagingData = response.fetchPagingData(section = 3) (selects the section 3 if Feed response, ignores ‘section’ if a search

The PagingDataModel has the following structure:
-	totalCount: Int
-	itemCount: Int
-	pageCount: Int
-	pageSize: Int
-	pageIndex: Int

CloudMatrixResponse  ItemDataModel
Similar to PagingData, ItemData can either be stored in a section with direct URLs and Feed responses, or in the root of the response for searches.
The SDK can also automatically provide this data, but if it is preferred that it should be handled manually, the correct item data can be retrieved from the root of CloudMatrixResponse by calling the ‘fetchResult(section:Int?)’ or the ‘fetchResults()’ methods/

val itemData = response.fetchResults() (selects all results returned by the API, even if split in sections)

val itemData = response. fetchResults(section = 3) (selects the section 3 if Feed response, ignores ‘section’ if a search)

The ItemDataModel has the following structure:
-	id: String?
-	mediaData: CloudMatrixMediaDataModel?
    o	       mediaType: String?,
    o	       entryId: String?,
    o	       entryStatus: String?,
    o	       thumbnailUrl: String?
-	metaData: CloudMatrixMetaDataModel? – See ‘MetaDataModel’ below
-   sortData: ArrayList<CloudMatrixSortDataModel>
    o	       feedId: String?,
    o	       sectionId: String?,
    o	       order: Int?
-	publicationData: CloudMatrixPublicationDataModel?
    o	       createdAt: String?,
    o	       updatedAt: String?,
    o	       released: Boolean?,
    o	       releaseFrom: String?,
    o	       releaseTo: String?

CloudMatrixResponse  MetaDataModel
The MetaDataModel does not follow a standard ‘model’ as the keys in the structure are customisable per-partner. Instead of a concrete data model, the object is purely a key / value HashMap (HashMap<String, Any>)
Although there is no defined structure to this object, some keys are guaranteed to exist, although there is no guarantee these will not be null, the list below contains these items and a convenience method (called on the ItemData, not the MetaData) to retrieve them.
-	title: String? – itemData.getTitle() – also: itemData.metaData.title
-	body: String? – itemData.getBody() – also: itemData.metaData.body
-	duration: Double? – itemData.getDuration() – also: itemData.metaData.duration
-	tags: Array<String>? – itemData.getTags() – also: itemData.metaData.tags

To access custom data, convenience methods to retrieve Strings, Integers and Arrays from the meta data are provided:

-	itemData.getMetaDataString(key: String): String? – also itemData.metaData.getString(key: String)
-	itemData.getMetaDataInt(key: String): Int? – also itemData.metaData.getInt(key: String)
-	itemData.getMetaDataLong(key: String): Long? – also itemData.metaData.getLong(key: String)
-	itemData.getMetaDataDouble(key: String): Double? – also itemData.metaData.getDouble(key: String)
-	itemData.getMetaDataBool(key: String): Bool? – also itemData.metaData.getBool(key: String)
-	itemData.getMetaDataArray(key: String): Array<Any>? – also itemData.metaData.getArray(key: String)
-	itemData.getMetaDataStringArray (key: String): Array<String>?  – also itemData.metaData.getStringArray (key: String)

##Extending the CloudMatrixMetaDataModel
If a key is frequently used, there is also the option to extend the ‘CloudMatrixMetaDataModel’ to return it as a parameter (Kotlin only)

For example, to create a property called ‘teams’ that returns the array held by the key ‘teams’ in MetaData, create a new Kotlin file and add the following:
**Kotlin:**
```kotlin
val CloudMatrixMetaDataModel.teams: ArrayList<String>?
    get() {
       return this.getStringArray("teams")
    }
```

You can also create more complex extensions, for example, to access a single string concatenating all the strings from the array held by the ‘teams’ key:
**Kotlin:**
```kotlin
fun CloudMatrixMetaDataModel.fetchTeams(): String{
    var allTeams = "No Teams!"
    var arrayOfTeams = this.getStringArray("teams")
    arrayOfTeams?.let {
        if (it.isNotEmpty()) {
            returnString = "Teams:\n\n"
            for (team: String in it) {
                returnString += "$team\n"
            }
        }
    }
    return allTeams
}
```

##Callbacks in CloudMatrix
To access the data returned by the SDK, a callback is required for each request. This callback can either be added in line to an individual request, or a defined callback can be added.

The callback is required in the form:
((CloudMatrixResponse?, StreamAMGError?) -> Unit)?

It can be added inline:
**Kotlin:**
```kotlin
streamPlay.callAPI(request) { response, error ->
     if (error == null) {
         // The ‘response’ object of type ‘CloudMatrixResponse?’ is available here
response?.let	{ useableResponse ->
// Do something with the response model
}
     			} else 	{
// Do something with the error model
     				}
 			}
}
```

Or as a reusable parameter:
**Kotlin:**
```kotlin
val cloudMatrixCallback: ((StreamPlayResponse?, StreamAMGError?) -> Unit)? = { response, error ->
     if (error == null) {
         // The ‘response’ object of type ‘CloudMatrixResponse?’ is available here
response?.let	{ useableResponse ->
// Do something with the response model
}
     			} else 	{
// Do something with the error model
     				}
 			}
}
```

streamPlay.callAPI(request, cloudMatrixCallback)

Paging in StreamSDK-CloudMatrix
The CloudMatrix module handles paging for any responses received. By default the API will return 200 records by page, but this can be configured in any request by either passing a Builder option or during initialisation

Using Builder:
**Kotlin:**
```kotlin
.paginateBy(paginateBy: Int)
```


Using standard initialisation
**Kotlin:**
```kotlin
val feed = CloudMatrixRequest(paginateBy = (items per page as Int))
```

Paging data can be called via the response model to enable / disable paging buttons / pull to refresh, etc
The current request can be paginated using the following methods:
Previous page
**Kotlin:**
```kotlin
CloudMatrix.loadPreviousPage()
```

Next page
**Kotlin:**
```kotlin
CloudMatrix.loadNextPage()
```


##Currently available data
The accepted valid fields for StreamPlay are as follows:
**Kotlin:**
```kotlin
enum CloudMatrixQueryType{
ID
MEDIATYPE
ENTRYID
ENTRYSTATUS
THUMBNAILURL
BODYTEXT
VIDEODURATION
TITLETEXT
TAGS
CREATEDDATE
UPDATEDDATE
RELEASED
RELEASEFROM
RELEASETO
}

```








Change Log:
===========

All notable changes to this project will be documented in this section.

### 1.0.1 - Release

### Beta releases

### 0.2 -> 0.4 - No changes to CloudMatrix

### 0.1 - Initial build
