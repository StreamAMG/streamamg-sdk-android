#  AMG PlayKit Library

The AMG PlayKit library is a simple to use wrapper around the Kaltura PlayKit suite. It provides a single Android View (AMGPlayKit) with which to play and interact with standard AMG provided media streams, whilst automatically taking care of media analytics, Google IMA (media advertising), basic casting and basic player UI.

## Installing AMG PlayKit

Add the jitpack repository and the Youbora repository to your project level build.gradle

The Youbora repository is currently required even if you are not implementing Youbora analytics. It is hoped this will be resolved in a future releasae.

```
allprojects {
       repositories {
           ....
           maven { url "https://jitpack.io" }
           maven { url  "https://npaw.jfrog.io/artifactory/youbora/" }
       }
  }
```

In your app level build.gradle file, add the dependencies required

```  
    implementation "com.github.StreamAMG.streamamg-sdk-android:streamamg-sdk-playkit:(version number)"
```  

Sync your Gradle files, and the AMGPlayKit should import and be available for use.

## Getting Started

Once the library is installed, you can add AMG PlayKit to your project either programatically, or via layout XML.

The class a developer would interact with is simply called 'AMGPlayKit', this single class provides all standard functions of the PlayKit and will be used for the vast majority of interactions with the PlayKit

### Programatic use

To instantiate an instance of AMGPlaykit, the following constructor should be called:

``` Kotlin
constructor(context: Context, partnerID: Int, analytics: AMGAnalyticsConfig? = null)
```
for example :

``` Kotlin
val analyticsConfig = AMGAnalyticsConfig(youboraAccountCode: "youbora_account_code") // Optional
val playKit = AMGPlayKit(context, 1111111, analyticsConfig)
```
You can also initialise the PlayKit without a PartnerID
``` Kotlin
constructor(context: Context, analytics: AMGAnalyticsConfig? = null)
```
But you will be required to send the PartnerID separately to play media.

### layout XML use

To instantiate via xml, you simply need to add the view to your XML file as you would with any native Android view

``` xml
....
       app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintVertical_chainStyle="packed" />

    <com.streamamg.amg_playkit.AMGPlayKit
        android:id="@+id/playkit"
        android:layout_width="0px"
        android:layout_height="wrap_content"
        app:partner_id="@integer/partner_id"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/textview_first"
        app:use_standard_controls="false"/>

    <LinearLayout
        android:id="@+id/custom_controls"
        android:layout_width="0px"
....
```

You can add the partner_ID in the xml node, as well as defining the use of the standard controls:

``` xml
app:use_standard_controls="false" //true will enable the standard overlayed UI controls
app:partner_id="@integer/partner_id" //an integer defining the partner ID to be used in the player
```

The following setup code should be carried out after connecting to the player in code:

Create the player:

``` Kotlin
playKit.createPlayer(analytics: AMGAnalyticsConfig? = null)
```

### Managing the app lifecycle

To correctly serve media and adverts, certain parts of the lifecycle of the app should be passed through to the SDK:

Pausing the player should be managed whenever the app enters the background or the fragment or activity the player is hosted on is paused:

``` Kotlin
    playKit.playerPause()
```

And resuming the player should happen when the fragment or activity is resumed (or the app enters the foreground)

``` Kotlin
    playKit.playerResume()
```

When PlayKit is no longer required, it should be destroyed. This removed all active PlayKit and Player operations (but does not affect PlayKit2Go downloads)

``` Kotlin
    playKit.destroyPlayer()
```

### Adding an analytics service

Currently Playkit supports 2 anayltics services, StreamAMG's own Media Player analyics service, and Youbora analytics.
To use either service, you should instantiate the player with a configuration model:

``` Kotlin
constructor(youboraAccountCode: String)
```

or

``` Kotlin
constructor(amgAnalyticsPartnerID: Int)
```

If you do not pass an analytics configuration during initialisation, no analytics service will be used

Youbora analytics can handle up to 20 extra static parameters being passed to it. To do this, you should use the YouboraService builder class:

``` Kotlin
        var analyticsConfig = AMGAnalyticsConfig.YouboraService()
            .accountCode("streamamgdev") // REQUIRED
            .userName("A USER CODE") // Should be non identifying
            .parameter(1, "Static Parameter Value 1")
            .parameter(2, "Static Parameter Value 2")
            .parameter(3, "Static Parameter Value 3") // through to 20
            .build()
        amgPlayKit.createPlayer(requireContext(), analyticsConfig)
```

If you do not pass an account code in this instance, the configuration file will not work

### Manually updating the PartnerID

PartnerID can be added or changed programatically with the function
```
public fun addPartnerID(partnerId: Int)
```

It should be noted that you cam also send a new PartnerID with any new media sent.

## Standard Media controls

A set of UI Controls are provided as standard for the Play Kit, but these are not enabled by default.

To programmatically allow the basic configuration of the controls to be used (overlayed on the player itself), simple add the following line to your Play Kit set up code:

``` Kotlin
playKit.addStandardControl()
```

This adds a UI that appears when the user touches the Play Kit window, and has the following characteristics:

- Scrub bar is positioned at the bottom of the player
- The play state is NOT toggled when the user reveals the controls
- The controls disappear after 5 seconds of no interaction
- Skip forward and backward buttons skip 5 seconds

You can control some of these defaults programatically:

Set the skip forward time:

``` Kotlin
playKit.skipForwardTime(_ duration: Int) // in milliseconds (eg, 5250)
```

Set the skip backward time:

``` Kotlin
playKit.skipBackwardTime(_ duration: Int) // in milliseconds (eg, 5250)
```

Set the skip forward and backward time:

``` Kotlin
playKit.skipTime(_ duration: Int) // in milliseconds (eg, 5250)
```

It is also possible to configure these settings by using the AMGControlBuilder class.

``` Kotlin
val controls = AMGControlBuilder()
    .setHideDelay(2500) // sets the delay of inactivity to 2.5 seconds (2500 Milliseconds) before hiding the controls
    .setTrackTimeShowing(true) // Shows the start and end times, configured depending on the visability of the current time
    .build()

    playKit.addStandardControl(config: controls)
```

The following options are available with the builder:

Set the delay, in milliseconds, of the inactivity timer before hiding the controls
``` Kotlin
.setHideDelay(time: Int)
```

Toggle the visibility of the current track time
``` Kotlin
.setTrackTimeShowing(isOn: Bool)
```
Set the time, in milliseconds, of skip forward / backward controls
   ``` Kotlin
   .setSkipTime(time: Long)
   ```

Set the time, in milliseconds, of skip forward control
   ``` Kotlin
   .setSkipForwardTime(time: Long)
   ```

Set the time, in milliseconds, of skip backward control
   ``` Kotlin
   .setSkipBackwardTime(time: Long)
   ```

Hide the 'fullscreen' button
   ``` Kotlin
   .hideFullScreenButton()
   ```

Hide the 'minimise' button when the player is in full screen
   ``` Kotlin
   .hideMinimiseButton()
   ```

Specify the image to use for the play button
   ``` Kotlin
   .playImage(image: Int)
   ```

Specify the image to use for the pause button
   ``` Kotlin
   .pauseImage(image: Int)
   ```

Specify the image to use for the fullscreen button
   ``` Kotlin
   .fullScreenImage(image: Int)
   ```

Specify the image to use for the skip forwards button
   ``` Kotlin
   .skipForwardImage(image: Int)
   ```

Specify the image to use for the skip backward button
   ``` Kotlin
   .skipBackwardImage(image: Int)
   ```

Set the colour of the scrub bar 'tracked' time to a colour resouce (eg R.color.amg_blue)
``` Kotlin
.scrubBarColour(colour: Int)
```

Set the colour of the live scrub bar 'tracked' time to a colour resouce (eg R.color.amg_blue)
``` Kotlin
.scrubBarLiveColour(colour: Int)
```

Set the colour of the VOD scrub bar 'tracked' time to a colour resouce (eg R.color.amg_blue)
``` Kotlin
.scrubBarVODColour(colour: Int)
```

Toggle the visibility of the bitrate selector
``` Kotlin
.setBitrateSelector(isOn: Bool)
```

Toggle the visibility of the subtitles selector
``` Kotlin
.setSubtitlesSelector(_ isOn: Bool)
```


## Media overlays

AMG Play Kit supports the overlaying of an 'is live' badge and a logo as overlays to any media playing.

To specify the badges, use the following functions:
From a resource file
``` Kotlin
playKit.setIsLiveImage(R.drawable.is_live_image, atWidth = 100) //atWidth is an optional size in pixels (will be adjusted to dips) for the width of the image - height will be calculated automatically
                                                                // the default value is 70 pixels
```
and

``` Kotlin
playKit.setLogoImage(R.drawable.logo_image, atWidth = 100) //atWidth is an optional size in pixels (will be adjusted to dips) for the width of the image - height will be calculated automatically
                                                              // the default value is 70 pixels
```

From a URL
``` Kotlin
playKit.setIsLiveImage(url = (valid URL of the image), atWidth = 100, atHeight = 50) // atWidth and atHeight are size in pixels (will be adjusted to dips)
```
and

``` Kotlin
playKit.setLogoImage(url = (valid URL of the image), atWidth = 100, atHeight = 50) // atWidth and atHeight are size in pixels (will be adjusted to dips)
```

To show and hide these overlays, use these functions:

``` Kotlin
playKit.setiSliveImageShowing(true) // playKit.setiSliveImageShowing(false)
```

and

``` Kotlin
playKit.setlogoImageShowing(true) // playKit.setlogoImageShowing(false)
```


## Custom Media Controls

You can also provide your own media controls either as an overlay on the player, or as a separate component.

An example class is provided here (all components are added in an xml file, but could also be added programmatically):

``` Kotlin

class ExamplePlayer : Fragment(), AMGControlInterface {
    val entryID = "0_xxxxxxx"
    val PARTNER_ID = 11111111
    val SERVER_URL = "https://mp.streamamg.com"

    lateinit var playpause: TextView // Play state toggle
    lateinit var slider: Slider // Scrub bar
    var player: AMGPlayerInterface? = null

    lateinit var amgPlayKit: AMGPlayKit

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_first, container, false)
        playpause = view.findViewById(R.id.custom_play_pause)
        slider = view.findViewById(R.id.custom_slider)
        playpause.setOnClickListener {
            if (player?.playState() == AMGPlayKitPlayState.playing){
                player?.pause()
            } else {
                player?.play()
            }
        }

        slider.addOnChangeListener { slider, value, fromUser ->
            if (fromUser) {
                player?.scrub((value * 1000).toLong())
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        amgPlayKit = view.findViewById(R.id.playkit)
        amgPlayKit.setControlInterface(this) // Define the interface for the controller in the player
        player = amgPlayKit // set the player as the interface for recieving call backs
        loadMedia()
    }

// In this instance, we define the play state only when we have confirmation from the player that the play state has changed

    override fun play() {
        playpause.setText(R.string.pause)
    }

    override fun pause() {
        playpause.setText(R.string.play)
    }

// This function moves the scrub bar thumb to the correct position, keeping track of the playhead automatically
    override fun changePlayHead(position: Long) {
        slider.value = (position/1000).toFloat()
    }

// When the media is changed, we must ensure the scrub bar data is made up to date.
    override fun changeMediaLength(length: Long) {
        if (length <= 1000){
            slider.valueTo = 1.0f
        } else {
            slider.valueTo = (length / 1000).toFloat()
        }
        duration = length
        slider.valueFrom = 0f
        slider.value = 0f
    }

    fun loadMedia(){
        amgPlayKit.loadMedia(entryID, PARTNER_ID, SERVER_URL)
    }
}
```

You should accept an interface of type 'AMGPlayerDelegate' this should be the player object itself:

``` Kotlin
public interface AMGPlayerInterface {
    fun play()
    fun pause()
    fun scrub(position: TimeInterval)
    fun setControlDelegate(delegate: AMGControlDelegate)
    fun cancelTimer()
    fun startControlVisibilityTimer()
    fun playState(): AMGPlayKitPlayState
}
```

play, pause and scrub(position:) control the state of the player

setControlDelegate(delegate:) will change the delegate of the control receiver to whichever class you specify (must conform to AMGControlInterface).

cancelTimer and startControlVisibilityTimer are used when overlaying the player with your controls and determine the visibility of the controls

playState returns the current state of the player (either playing or paused)

To use this control class, you should add it to your Play Kit set up code, for example, in the above class:

``` Kotlin
       override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
           super.onViewCreated(view, savedInstanceState)
           amgPlayKit = view.findViewById(R.id.playkit)
           amgPlayKit.setControlInterface(this) // Define the interface for the controller in the player
           player = amgPlayKit // set the player as the interface for recieving call backs
           loadMedia()
       }
```

## Play Kit orientation

The AMG Play Kit can be displayed in portrait mode or full screen landscape mode.

The app itself must handle the change in view (either via XML or programmatically), as well as instructing the Play Kit on the desired orientation.

For smooth orientation changes without restarting the current activity, the orientation changes should be specified in the app manifest:

```
....
  <activity
            android:name=".YourActivity"
            android:label="@string/app_name"
            android:configChanges="orientation|screenSize|keyboardHidden">
....
```

To infer orientation to the Play Kit, you should call the Play Kit's

``` Kotlin
   playKit.initialiseSensor(activity, true) // activity is a non null valid Activity reference
```

As well as (or instead of) changing via a physical orientation change, you can use the 'fullscreen' button on the Play Kit Standard Control UI - this appears, unless it is disabled, in the bottom right corner of the Play Kit view.

To disable this completely, use the '.hideFullScreenButton()' builder function when creating the Control UI configuration

The full screen button can also be disabled only when the Play Kit is full screen using the '.hideFullScreenButtonOnFullScreen()' instead.

If using a non standard Control UI, you can simply call the following functions to implement your own full screen button:

For fullscreen
``` Kotlin
playKit.fullScreen()
```
To minimise from full screen
``` Kotlin
playKit.minimise()
```

If you are running PlayKit in a wider activity, as opposed to it's own activity (in a Fragment, for example), you should remove orientation when leaving the player screen (assuming you do not want the rest of the activity to be able to re-orient)

``` Kotlin
   playKit.initialiseSensor(activity, false) // activity is a non null valid Activity reference
```
## Sending Media

The main function of PlayKit is to play and interact with media provided by Stream AMG and it's partners.

There are only 5 required elements when requesting media to be played:
* Partner ID
* Media URL
* Entry ID
* KS (where needed)
* Title (For Youbora analytics)

you can also pass a 'mediaType' element to force the player into 'live' mode if required - see below

Please note it is no longer required to pass the UIConfig parameter to PlayKit.

If you have provided the Partner ID to the PlayKit already, you do not need to pass this with each media request:

``` Kotlin
public fun loadMedia(serverUrl: String, entryID: String, ks: String? = null, title: String? = null, mediaType: AMGMediaType = AMGMediaType.VOD, startPosition: Long = -1)
```
for example:
``` Kotlin
playKit.loadMedia("https://mymediaserver.com", "0_myEntryID", "VALID_KS_PROVIDED_BY_STREAM_AMG", title: String? = null)
```

Or with a Partner ID
``` Kotlin
public fun loadMedia(serverUrl: String, partnerID: Int, entryID: String, ks: String? = null, title: String? = null, mediaType: AMGMediaType = AMGMediaType.VOD, startPosition: Long = -1)
```
for example:
``` Kotlin
playKit.loadMedia("https://mymediaserver.com", 111111111, "0_myEntryID", "VALID_KS_PROVIDED_BY_STREAM_AMG", "AMG Demo Video")
```

If the media does not require a KSession token, this should be left as null

### Forcing live mode

When sending media to the player, the mediaType defaults to VOD and will automatically attempt to determine if the media is live or VOD, this will affect the scrub bar colours (if they are different) and the layout of the scrub bar.

If the media is a 'harvested' live event (such as a replay), the VOD scrub bar should show

To force the player into 'live' mode, 'mediaType: .Live' should be passed to the player when sending media

``` Kotlin
playKit.loadMedia("https://mymediaserver.com", 111111111, "0_myEntryID", "VALID_KS_PROVIDED_BY_STREAM_AMG", AMGMediaType.Live)
```

Currently the player is either 'Live' or 'VOD'

### Casting URL

To access the casting URL of the currently playing media use the following function:

``` Kotlin
playKit.castingURL(format: AMGMediaFormat = AMGMediaFormat.HLS, completion: (URL?) -> Unit)
```
The completion returns a fully qualified casting URL (or null on error)

for example:
``` Kotlin
playKit.castingURL(AMGMediaFormat.HLS) { url ->
    // Work with HLS URL here
}
```

Media format is either `AMGMediaFormat.HLS` or `AMGMediaFormat.MP4` - Defaults to HLS

You can also pass media data to a separate function to return either a valid URL or null without needing to play the media in app:

``` Kotlin
playKit.castingURL(server: String, partnerID: Int, entryID: String, ks: String? = null, format: AMGMediaFormat = AMGMediaFormat.HLS, completion: (URL?) -> Unit)
```

### Serving Adverts

AMG PlayKit supports VAST URL adverts.

To serve an advert pre, during or post media, send the VAST URL to the following function

``` Kotlin
public fun serveAdvert(adTagUrl: String)
```
for example:
``` Kotlin
playKit.serveAdvert("VAST_URL_FOR_REQUIRED_ADVERT")
```

### Spoiler Free

PlayKit has the ability to hide the scrub bar and timing lables, effectively making the video 'spoiler free'

To enable (or disable) spoiler free mode:

``` Kotlin
playKit.setSpoilerFree(enabled: Boolean) // true = spoiler free mode on, false = scrub bar on
```

### Bitrate Selection

To instruct PlayKit to use a certain highest bitrate when streaming, you can use the following function:

``` Kotlin
playKit.setMaximumBitrate(bitrate: FlavorAsset?)
```

PlayKit will atttempt to change bitrate to that value (or the closest one BELOW that value) for the rest of the stream. This change may not be immediate.

PlayKit has a listener (`AMGPlayKitListener`) that contains a method (`bitrateChangeOccurred`) that gives you the list of bitrate when available:
``` Kotlin
override fun bitrateChangeOccurred(list: List<FlavorAsset>?) {

}
```

### Media Volume / Mute option

To set media volume or set mute option, you can use the setVolume function of the player:

``` Kotlin
playKit.player?.setVolume(volume: float) // 0.0F = mute, 1.0F = full volume
```
for example:
``` Kotlin
playKit.player?.setVolume(0.5F)
```

### Track and Subtitle Selection

To instruct PlayKit to use a certain track when streaming (if available), you can use the following function:

``` Kotlin
playKit.changeTrack(id: String)
```

PlayKit will atttempt to change the track to the chosen one for the rest of the stream. The id of the track should be the uniqueId of the available MediaTrack

PlayKit has a listener (`AMGPlayKitListener`) that contains a method (`tracksAvailable`) that gives you the list of available `MediaTrack` including the text tracks (subtitle) when ready:
``` Kotlin
override fun tracksAvailable(tracks: List<MediaTrack>) {

}
```

Once the tracks are available and ready, is possible to filter and select the chosen track in this way:
``` Kotlin
tracks.firstOrNull { it.language?.contains("english") == true && it.type == TrackType.TEXT }?.let { track ->
    playKit.changeTrack(track.uniqueId)
}
```

Hovewer, PlayKit will take care to select the default caption track once the video will be loaded.

# Change Log

All notable changes to this project will be documented in this section.

### 1.2.0
- Default subtitle track auto-selected
- Get Label caption on subtitle selector

### 1.1.8
- Subtitles UI selector

### 1.1.7
- Subtitle feature (no UI integration)
- Added user location to AMG Analytics Plugin
- Updated PlayKit libraries

### 1.1.6
- Bitrate listener added
- Fixed and improved AMG Analytics

### 1.1.5
- IAP enhanced raw receipt

### 1.1.4
- PlayKit fixed Attempt to invoke 'getCurrentPosition()' on null instance of the player engine

### 1.1.3
- AMG SDK: Update PlayKit to the version v4.23.0
- Exposed PlayKit property

### 1.1.2
- Fixed chromecasting crash when offline
- Fixed bitrate selector crash when empty list

### 1.1.1
- Added startPosition to loadMedia
- Fixed bitrate selector icon when live

### 1.1.0
- Improved bitrate selector UI
- Playkit updates
- Modified analytics default url to https

### 1.0.3
- Force casting URL to return a redirected URL if required (HLS only)
- Change call to require completion

### 1.0.2
- Tidied standard control duration field

### 1.0.1
- Play harvested content as VODs
- Minor design change to standard UI
- Added bitrate selector

### 1.0 - Release

### Beta releases

### 0.4 - Minor changes for PlayKit2Go integration

### 0.3
- Added Youbora analytics and the ability to choose analytics services
- Added ability to change scrub bar colours
- Automatically detect live streams
- Tidied Custom Control builder
- Small bug fixes

### 0.2 PlayKit bug fixes

### 0.1 Initial build
