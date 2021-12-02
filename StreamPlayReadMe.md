
StreamSDK StreamPlay Module
=====================
The StreamPlay SDK provides data and information concerning video and other media available to partners (and internally) by harnessing the power of the StreamPlay API in a simple, easier to consume form.

Although not as extensive as the CloudMatrix API, StreamPlay is simpler to use and returns a consistent guaranteed response that requires little to no additional set up to use.

The following requests can be made using the StreamPlay SDK:

FEED – which returns a pre-defined data set back for immediate consumption

SEARCH – which allows filtering and searching of all available data for the partner

A single model (StreamPlayResponseModel) is returned after a successful transaction, otherwise a StreamAMGError is returned explaining any issues encountered.

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

In your app level build.gradle file, add the dependencies required - you MUST add core for StreamPlay

```  
    implementation "com.github.StreamAMG.streamamg-sdk-android:streamamg-sdk-core:(version number)"
    implementation "com.github.StreamAMG.streamamg-sdk-android:streamamg-sdk-streamplay:(version number)"
```  


API Overview
============

##Setting up StreamPlay
The StreamPlay SDK must be instantiated to allow requests to be made through it.
**Kotlin:**
```kotlin
Class StreamPlay(val partnerID: String? = null, val sport: ArrayList<StreamPlaySport> = ArrayList())
```


For example:
**Kotlin:**
```kotlin
val streamPlay: StreamPlay = StreamPlay()
Would instantiate a StreamPlay object with default settings
```


Unlike the CloudMatrix SDK, the StreamPlay SDK does not need default data passed to it on instantiation, as all data can be passed with requests, it is, however, possible to create an initialisation to create default data to be sent with each report.
**Kotlin:**
```kotlin
val streamPlay: StreamPlay = StreamPlay(partnered = ”PARTNER_ID”, sport = arrayListOf(StreamPlaySport.FOOTBALL, StreamPlaySport.BASKETBALL))
```


Multiple objects can be instantiated to use multiple feeds simultaneously.

The API will use either the production or staging URL depending on the StreamSDK Environment

##Making API Requests
The StreamPlay SDK work in a very similar fashion to the CloudMatrix SDK, in that either a standard object can be created and updated for the request, or, as is preferred, a builder can be used.

Once a request has been made and delivered, the SDK will return a valid response model, or an error object to a callback specified. The responses and errors are discussed later in this document.

Requests are created separately from the StreamPlay object and then passed to the Core Module via the StreamPlay object.

Once again, a callback is, technically, optional, but currently there is no other way of accessing the response.

 
##Sending a request to Core
All StreamPlay SDK requests are handled by the Core module. This is managed in 2 steps, the request construction and the request delivery.
**Kotlin:**
```kotlin
val request = (StreamPlayRequestModel) – see below for details on constructing the request

streamPlay.callAPI(request, _streamplayclosure_) – see below for details on constructing the closure
```


If a request is successful, a StreamPlayResponseModel will be delivered (via the closure), otherwise an error will be returned.

##Accessing Static Feeds
Using pre-defined complete URLs
If an app receives a config file with given static StreamPlay URLs (or if these URLs are known to be static and can be included as hard coded strings), then the StreamPlay SDK can handle these URLs and provide a standard response model for consumption.

To re-iterate the advantages of using the SDK for even this most simple of tasks:
-	Networking is handled
-	Errors are handled
-	A standard model is used for the response
-	Paging, if necessary, is handled

Because these feeds should already contain all necessary data in them (including the partner ID, sport and fixture IDs, etc) then only a very simple request is needed:
**Kotlin:**
```kotlin
val request = StreamPlayRequest
.FeedBuilder()
.url("https://api.streamplay.streamamg.com/fixtures/football/p/3001343")
.build()
```


Or
**Kotlin:**
```kotlin
val request = StreamPlayRequest(url = "https://api.streamplay.streamamg.com/fixtures/football/p/3001343")
```


In this instance, the builder is less of a boon to use, it’s usefulness is far more evident in most of the other requests.

##Building a feed manually
In certain instances, it may be required to manually create a feed, this, similarly, can be done either using a builder or standard object methods.
As an object:
**Kotlin:**
```kotlin
Class StreamPlayRequest(
        val sport: ArrayList<StreamPlaySport>, //(Required if not sent in instantiation)
        var fixtureID: String? = null,
        var partnerID: String? = null, //(Required if not sent in instantiation)
        var params: ArrayList<SearchParameter> = ArrayList(), //(see additional details below)
        var url: String? = null,
        var paginateBy: Int = 0)
```


for example:
**Kotlin:**
```kotlin
var request = StreamPlayRequest(
sport = arrayListOf(StreamPlaySport.FOOTBALL, StreamPlaySport.BASKETBALL),
fixtureID = “Fixture ID”,
partnerID=”Partner ID”
```

)


Using a builder:
**Kotlin:**
```kotlin
var request = StreamPlayRequest
        .FeedBuilder()
        .fixture("Fixture ID")
        .partner("Partner ID")
        .sports(arrayListOf(StreamPlaySport.FOOTBALL, StreamPlaySport.BASKETBALL))
        .fixture("Fixture ID")
        .build()
```



Extra detail
Additional details can be added to this request if required:
**Kotlin:**
```kotlin
request.paginateBy = 15
```


##Using the StreamSDK-StreamPlay Search Capabilities
The search capabilities for StreamPlay vary slightly from CloudMatrix, but the range of search types is still vast.
**Kotlin:**
```kotlin
val search = StreamPlayRequest
.SearchBuilder()
        .sport(StreamPlaySport.FOOTBALL)
        .partner("3001343")
        .isLike(StreamPlayQueryField.FIXTURE_NAME, "West")
        .build()
```


There is no limit to the amount of parameters that can be included, or to the types of searches that can be mixed.

Currently there is only ‘AND’ searches, but ‘OR’ is being worked on.

The build components are flexible:
**Kotlin:**
```kotlin
isEqualTo(target: StreamPlayQueryField, query: String)

isEqualTo(target: StreamPlayQueryField, query: Number)
```


The ‘target’ is the field in the database being referenced, the ‘query’ is the item being searched for

The following query types are available:

Exact match of word or numbers
**Kotlin:**
```kotlin
.isEqualTo(target: StreamPlayQueryField, query: String)
```


Value is greater than (or equal to) the query. This can be passed as a String or Number
**Kotlin:**
```kotlin
.isGreaterThan(target: StreamPlayQueryField, query: String)
.isGreaterThanOrEqualTo(target: StreamPlayQueryField, query: String)
```


Value is less than (or equal to) the query. This can be passed as a String or Number
**Kotlin:**
```kotlin
.isLessThan(target: StreamPlayQueryField, query: String)
.isLessThanOrEqualTo(target: StreamPlayQueryField, query: String)
```


Fuzzy search – “foot” will match ‘right-footed’, ‘football’ and ‘foot’, etc
**Kotlin:**
```kotlin
.isLike(target: StreamPlayQueryField, query: String)
```


Starting character search – “foot” will match ‘football’ and ‘foot’, but not ‘right-footed’
**Kotlin:**
```kotlin
.startsWith(target: StreamPlayQueryField, query: String)
```


The following searches require no ‘query’

Boolean searches on a field
**Kotlin:**
```kotlin
.isTrue(target: StreamPlayQueryField)
.isFalse(target: StreamPlayQueryField)
```


Sort order by field
**Kotlin:**
```kotlin
.sortByAscending(target: StreamPlayQueryField)
.sortByDescending(target: StreamPlayQueryField)
```


Set date range of query (Date format is “YYYY-MM-DD”
**Kotlin:**
```kotlin
.dateFrom(date: String)
.dateTo(date: String)
```


Set whether the Start date or End date of the fixture is used in the range query
**Kotlin:**
```kotlin
.endDateEffective() (Default)
.startDateEffective()
```

##Batch processing

In a normal call to the server, you will likely want the response to be immediately delivered back to the callback so the app can respond accordingly. There may be occasions, however, where it would be preferable for multiple jobs to complete before processing.

In these situations, the StreamSDKBatchJob service can collate any number of jobs, from either a single module or a mixture of any modules, make a request from the API and hold the responses until all jobs have been completed.

Once all jobs are complete, the service will then fire all callbacks.
**Kotlin:**
```kotlin
        val search1 = StreamPlayRequest
                .FeedBuilder()
                .url(staticURL1)
                .build()

        val search2 = StreamPlayRequest
                .FeedBuilder()
                .url(staticurl2)
                .build()

val queue = StreamSDKBatchJob()
        queue.add(StreamPlayJob(search1, spBatchClosure))
        queue.add(StreamPlayJob(search2, spBatchClosure))
        ....
        queue.fireBatch()
```

The batch job can be fired as many times as required once created, but will not allow a restart until any running batch has completed

##The StreamPlay Response Model
Accessing retrieved data
When a successful call to the API has been returned, the sdk makes available a data model of type ‘StreamPlayResponse?’. This model contains the following information:

StreamPlayResponse root:
-	fixtures: ArrayList<FixturesModel>
-	total: Int
-	limit: Int
-	offset: Int

StreamPlayResponse FixturesModel:
Any response is guaranteed to have a FixturesModel array, although this may be empty if no results are retrieved. This model is fixed and should be extended.
-	       id: Int?
-	       type: String?
-	       partnerId: Int?
-	       featured: Boolean?
-	       name: String?
-	       description: String?
-	       startDate: String?
-	       endDate: String?
-	       createdAt: String?
-	       updatedAt: String?
-	       videoDuration: Int?
-	       externalIds: ExternalIDModel?
o	optaFixtureId: Int?
o	paFixtureId: Int?
o	sportsradarFixtureId: Int?      
-	       season: FixtureDetailModel? – See ‘FixtureDetailModel’ below
-	       competition: FixtureDetailModel?
-	       homeTeam: FixtureDetailModel?
-	       awayTeam: FixtureDetailModel?
-	       stadium: FixtureDetailModel?
-	       mediaData: ArrayList<ScheduleMediaDataModel> = ArrayList()
o	   mediaType: String?
o	   entryId: String?
o	   isLiveUrl: String?
o	   isLiveTime: Long?
o	   thumbnailUrl: String?
o	   drm: Boolean?
-	       thumbnail: String?,
-	       thumbnailFlavors: FixtureThumbnailFlavorsModel
o	      logo250: String?
o	      logo640: String?
o	      logo1024: String?
o	      logo1920: String?,
o	      source: String?

StreamPlayResponse FixtureDetailModel:
-	      id: Int?
-	      name: String?
-	      logo: String?
-	      logoFlavours: FixtureDetailLogoFlavorsModel?
o	      logo50: String?
o	      logo100: String?
o	      logo200: String?
o	      logo300: String?,
o	      source: String?


##Callbacks in StreamPlay
To access the data returned by the SDK, a callback is required for each request. This callback can either be added in line to an individual request, or a defined callback can be added.

The callback is required in the form:
**Kotlin:**
```kotlin
((StreamPlayResponse?, StreamAMGError?) -> Unit)?
```


It can be added inline:
**Java:**
```java
streamPlay.callAPI(request, (streamPlayResponse, streamAMGError) -> {

             if (streamAMGError == null) {
// Process the valid streamPlayResponse object (StreamPlayResponse) here
             } else {
// Process the valid streamAMGError object (StreamAMGError) here
}
     return null; // Must be called from java as the closure expects UNIT returned
         });
```
**Kotlin:**
```kotlin
streamPlay.callAPI(request) { streamPlayResponse, streamAMGError ->
     if (streamAMGError == null) {
         // The ‘streamPlayResponse’ object of type ‘StreamPlayResponse?’ is available here
streamPlayResponse?.let	{useableResponse ->
// Process the valid useableResponse object (StreamPlayResponse) here
}
     			} else 	{
// Process the valid streamAMGError object (StreamAMGError) here
     				}
 			}
}
```


Or as a reusable parameter:
**Kotlin:**
```kotlin
val streamPlayCallback: ((StreamPlayResponse?, StreamAMGError?) -> Unit)? = { streamPlayResponse, streamAMGError ->
     if (error == null) {
         // The ‘streamPlayResponse’ object of type ‘StreamPlayResponse?’ is available here
streamPlayResponse?.let	{useableResponse ->
// Do something with the useableResponse model
}
     			} else 	{
// Do something with the streamAMGError model
     				}
 			}
}

streamPlay.callAPI(request, streamPlayCallback)
```


##Paging in StreamSDK-StreamPlay
The StreamPlay module handles paging for any responses received. By default the API will return 20 records by page, but this can be configured in any request by either passing a Builder option or during initialisation

Using Builder:
**Kotlin:**
```kotlin
.paginateBy(paginateBy: Int)
```


Using standard initialisation
**Kotlin:**
```kotlin
val request = StreamPlayRequest(paginateBy = (items per page as Int))
```


Paging data can be called via the response model to enable / disable paging buttons / pull to refresh, etc.
The current request can be paginated using the following methods:
**Kotlin:**
```kotlin
streamPlay.loadPreviousPage()
```
**Kotlin:**
```kotlin
streamPlay.loadNextPage()
```

Currently available data
The accepted valid fields for StreamPlay are as follows:
Available sports:
enum StreamPlaySport {
FOOTBALL
BASKETBALL
RUGBY_LEAGUE
SNOOKER
POOL
DARTS
BOXING
GYMNASTICS
FISHING
NETBALL
TEN_PIN_BOWLING
PING_PONG
GOLF
}

Available query fields:
enum StreamPlayQueryField{
ID
MEDIA_TYPE
MEDIA_ENTRYID
MEDIA_DRM
FIXTURE_TYPE
FIXTURE_NAME
FIXTURE_DESCRIPTION
FIXTURE_OPTA_ID
FIXTURE_SPORTS_RADAR_ID
FIXTURE_PA_ID
SEASON_ID
SEASON_NAME
COMPETITION_ID
COMPETITION_NAME
HOME_TEAM_ID
HOME_TEAM_NAME
AWAY_TEAM_ID
AWAY_TEAM_NAME
STADIUM_ID
STADIUM_NAME
LOCATION_ID
LOCATION_NAME
EVENT_TYPE
}


Change Log:
===========

All notable changes to this project will be documented in this section.

### 1.0.1 - Release

### Beta releases

### 0.2 -> 0.4 - No changes to StreamPlay

### 0.1 - Initial build
