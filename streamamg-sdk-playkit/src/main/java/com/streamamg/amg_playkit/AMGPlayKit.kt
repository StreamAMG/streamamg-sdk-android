package com.streamamg.amg_playkit

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.OrientationEventListener
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.gson.JsonObject
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kaltura.playkit.*
import com.kaltura.playkit.player.*
import com.kaltura.playkit.plugins.ima.IMAConfig
import com.kaltura.playkit.plugins.ima.IMAPlugin
import com.kaltura.playkit.plugins.youbora.YouboraPlugin
import com.squareup.picasso.Picasso
import com.streamamg.amg_playkit.analytics.AMGAnalyticsConfig
import com.streamamg.amg_playkit.analytics.AMGAnalyticsPluginConfig
import com.streamamg.amg_playkit.analytics.AMGAnalyticsPlugin
import com.streamamg.amg_playkit.analytics.YouboraParameter
import com.streamamg.amg_playkit.constants.*
import com.streamamg.amg_playkit.controls.AMGPlayKitStandardControl
import com.streamamg.amg_playkit.interfaces.AMGControlInterface
import com.streamamg.amg_playkit.interfaces.AMGPlayKitListener
import com.streamamg.amg_playkit.interfaces.AMGPlayerInterface
import com.streamamg.amg_playkit.models.*
import com.streamamg.amg_playkit.network.PlayKitContextDataAPI
import com.streamamg.amg_playkit.network.PlayKitIsLiveAPI
import com.streamamg.amg_playkit.network.PlayKitTracksAPI
import com.streamamg.amg_playkit.playkitExtensions.*
import com.streamamg.amg_playkit.playkitExtensions.setBitrate
import com.streamamg.amg_playkit.playkitExtensions.updateBitrateSelector
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

/**
AMGPlayKit is an SDK that wraps Kaltura PlayKit, AMGAnalytics, IMA and other useful functions into a simple to use view.

The SDK, at it's most basic, is a LinearLayout, instantiated either programatically, or via XML, that acts as a single point of reference for all Kaltura PlayKit functionality
 */
class AMGPlayKit : LinearLayout, AMGPlayerInterface {

    private var playerShouldDestroy: Boolean = false
    var player: Player? = null
        private set
    private var playerState: PlayerState? = null
    lateinit var playerView: LinearLayout
    private var partnerId: Int = 0
    private var usingStandardControls: Boolean = false
    private var analyticsURL = "https://stats.mp.streamamg.com/SessionUpdate"
    private var analyticsMethod = AMGRequestMethod.GET
    private var analyticsHeaders = mapOf<String, String>()
    private var control: AMGControlInterface? = null
    lateinit var controlsView: AMGPlayKitStandardControl
    private var controlVisibleDuration: Long = 5000
    private var controlVisibleTimer: Timer? = null
    private var bitrateSelector: Boolean = false
    private var subtitleSelector: Boolean = false
    lateinit var isLiveImageView: ImageView
    lateinit var logoImageView: ImageView
    var isLiveImage: Int = 0
    var logoImage: Int = 0
    internal var skipForwardTime: Long = 5000
    internal var skipBackwardTime: Long = 5000
    var orientationActivity: Activity? = null

    internal var castingCompletion: ((URL?) -> Unit)? = null
    internal var castingURL: URL? = null
    internal var initialCastingURL: String? = null

    var isFullScreen = false

    var mSensorStateChanges: SensorStateChangeActions? = null
    var sensorEvent: OrientationEventListener? = null

    var listener: AMGPlayKitListener? = null

    var currentPlayHeadPosition: Long = 0

    var currentPlayerState: AMGPlayerState = AMGPlayerState.Stopped

    var currentMediaItem: MediaItem? = null

    internal var currentMediaType: AMGMediaType = AMGMediaType.VOD

    private lateinit var retroFitInstance: Retrofit
    lateinit var isLiveAPI: PlayKitIsLiveAPI
    lateinit var contextDataAPI: PlayKitContextDataAPI
    lateinit var tracksAPI: PlayKitTracksAPI

    var listBitrate: List<FlavorAsset>? = mutableListOf()
    var analyticsConfiguration = AMGAnalyticsConfig()

    internal var tracks: MutableList<MediaTrack>? = null

    /**
    Standard initialisation

    - Parameter context: The context of the activity or fragment the view is being placed in
    - Returns: A UIView containing an instantiated instance of the Kaltura PlayKit
     */
    constructor(context: Context, analytics: AMGAnalyticsConfig? = null) : super(context) {
        createPlayer(context, analytics)
    }

    /**
    Standard initialisation with Partner ID - The preferred programmatic initialisation

    - Parameter context: The context of the activity or fragment the view is being placed in
    - Parameter partnerID: An integer value representing the Partner ID to be used in any played media
    - Returns: A UIView containing an instantiated instance of the Kaltura PlayKit

    Partner ID can also be sent separately or as part of media data when loading media
     */
    constructor(context: Context, partnerID: Int, analytics: AMGAnalyticsConfig? = null) : super(context) {
        partnerId = partnerID
        createPlayer(context, analytics)
    }

    /**
    XML initialisation - Do not use this constructor
     */
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        setUpAttributes(context, attributeSet)
        //createPlayer(context)
    }

    fun destroyPlayer() {
        playerShouldDestroy = true
        pause()
        cancelTimer()
        playerView.removeAllViews()
        player?.destroy()
        player = null
        playerState = null
        control = null
        orientationActivity = null
        mSensorStateChanges = null
        sensorEvent= null
        listener = null
    }

    fun createPlayer(context: Context, analytics: AMGAnalyticsConfig? = null) {
        analytics?.let {analytics ->
            analyticsConfiguration = analytics
        }
        val inflater: LayoutInflater = LayoutInflater.from(context)
        var view = inflater.inflate(R.layout.playkit_view, this, true)
        playerView = view.findViewById(R.id.player_view)
        controlsView = view.findViewById(R.id.standard_controls)
        isLiveImageView = view.findViewById(R.id.is_live_image_view)
        logoImageView = view.findViewById(R.id.logo_image_view)
        setUpNetwork()
        setUpStandardControls()
        val plugins = createPlugins(context)
        player = PlayKitManager.loadPlayer(context, plugins)
        player?.let { player ->
            playerView.addView(player.view)
            player.addListener(this, PlayerEvent.stateChanged) { event ->
                checkForDestroy()
                playerState = event.newState
                var newState: AMGPlayerState? = when (playerState) {
                    PlayerState.IDLE -> AMGPlayerState.Idle
                    PlayerState.LOADING -> AMGPlayerState.Loading
                    PlayerState.READY -> AMGPlayerState.Ready
                    PlayerState.BUFFERING -> AMGPlayerState.Buffering
                    null -> null
                }

                newState?.let {
                    currentPlayerState = newState
                    listener?.loadChangeStateOccurred(AMGPlayKitState(it))
                }
            }

            player.addListener(this, PlayerEvent.error) { event ->
                errorOccurred(event)
                //    currentPlayerState = AMGPlayerState.Error
                if (event.error.errorType is PKPlayerErrorType) {
                    val error: AMGPlayerError = when (event.error.errorType as PKPlayerErrorType) {
                        PKPlayerErrorType.SOURCE_ERROR -> AMGPlayerError.SOURCE_ERROR
                        PKPlayerErrorType.RENDERER_ERROR -> AMGPlayerError.RENDERER_ERROR
                        PKPlayerErrorType.UNEXPECTED -> AMGPlayerError.UNEXPECTED
                        PKPlayerErrorType.SOURCE_SELECTION_FAILED -> AMGPlayerError.SOURCE_SELECTION_FAILED
                        PKPlayerErrorType.FAILED_TO_INITIALIZE_PLAYER -> AMGPlayerError.FAILED_TO_INITIALIZE_PLAYER
                        PKPlayerErrorType.DRM_ERROR -> AMGPlayerError.DRM_ERROR
                        PKPlayerErrorType.TRACK_SELECTION_FAILED -> AMGPlayerError.TRACK_SELECTION_FAILED
                        PKPlayerErrorType.LOAD_ERROR -> AMGPlayerError.LOAD_ERROR
                        PKPlayerErrorType.OUT_OF_MEMORY -> AMGPlayerError.OUT_OF_MEMORY
                        PKPlayerErrorType.REMOTE_COMPONENT_ERROR -> AMGPlayerError.REMOTE_COMPONENT_ERROR
                        PKPlayerErrorType.TIMEOUT -> AMGPlayerError.TIMEOUT
                        else -> AMGPlayerError.UNEXPECTED
                    }
                    listener?.errorOccurred(AMGPlayKitError(AMGPlayerState.Error, error.errorCode, error.name))
                }

            }

            player.addListener(this, PlayerEvent.play) { event ->
                playEventOccurred()
                listener?.playEventOccurred(AMGPlayKitState(AMGPlayerState.Play))
            }

            player.addListener(this, PlayerEvent.playing) { event ->
                playEventOccurred()
                listener?.playEventOccurred(AMGPlayKitState(AMGPlayerState.Playing))
            }

            player.addListener(this, PlayerEvent.pause) { event ->
                stopEventOccurred()
                listener?.stopEventOccurred(AMGPlayKitState(AMGPlayerState.Pause))
            }

            player.addListener(this, PlayerEvent.ended) { event ->
                stopEventOccurred()
                listener?.stopEventOccurred(AMGPlayKitState(AMGPlayerState.Ended))
            }

            player.addListener(this, PlayerEvent.durationChanged) { event ->
       //         stopEventOccurred()
                changeDuration(event.duration)
                currentPlayerState = AMGPlayerState.Loaded
                listener?.durationChangeOccurred(AMGPlayKitState(AMGPlayerState.Loaded, duration = event.duration))
            }

            player.addListener(this, PlayerEvent.playheadUpdated) { event ->
                setCurrentPlayhead()
            }

            player.addListener(this, PlayerEvent.tracksAvailable) { event ->
                tracks = event.tracksInfo.videoTracks.map { MediaTrack(it.uniqueId, TrackType.VIDEO, bitrate = it.bitrate, codecName = it.codecName, width = it.width, height = it.height) }.toMutableList()
                tracks?.addAll(event.tracksInfo.textTracks.map { MediaTrack(it.uniqueId, TrackType.TEXT, it.language, it.label, it.mimeType) })
                tracks?.addAll(event.tracksInfo.audioTracks.map { MediaTrack(it.uniqueId, TrackType.AUDIO, it.language, it.label, codecName = it.codecName, bitrate = it.bitrate, channelCount = it.channelCount) })
                tracks?.addAll(event.tracksInfo.imageTracks.map { MediaTrack(it.uniqueId, TrackType.IMAGE, url = it.url, bitrate = it.bitrate, duration = it.duration, label = it.label, cols = it.cols, rows = it.rows, width = it.width.toInt(), height = it.height.toInt()) })
                tracks?.let { tracks ->
                    tracks.filter { it.type == TrackType.TEXT }.let { textTracks ->
                        checkDefaultCaptionTrack(textTracks)
                        controlsView.createSubtitlesSelector(textTracks)
                    }
                    listener?.tracksAvailable(tracks)
                }
            }

            player.view?.isClickable = false

            player.settings.setAdAutoPlayOnResume(true)

        }
        playerView.setOnClickListener {
            bringControlsToForeground()
            if (usingStandardControls) {
                controlsView.toggleBitrateSelector(true)
                controlsView.toggleSubtitleSelector(true)
            }
        }

        controlsView.addOnLayoutChangeListener(object : OnLayoutChangeListener {
            override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                isFullScreen = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                controlsView.hideFullScreenButton(if (isFullScreen) 0 else 1)
            }
        })
    }

    private fun checkDefaultCaptionTrack(textTracks: List<MediaTrack>) {
        controlsView.selectedCaption = 0
        currentMediaItem?.captionAsset?.objects?.let {
            for (caption in it) { // Find the Label first
                if (caption.isDefault == true) {
                    textTracks.indexOfFirst { mediaTrack ->
                        mediaTrack.label == caption.label
                    }.let { defaultIndexCaptionTrack ->
                        if (defaultIndexCaptionTrack >= 0) {
                            player?.changeTrack(textTracks[defaultIndexCaptionTrack].uniqueId)
                            controlsView.setCaptionOnSelector(defaultIndexCaptionTrack)
                            return
                        }
                    }
                }
            }
            for (caption in it) { // As fallback, find the Language if Label not set
                if (caption.isDefault == true) {
                    textTracks.indexOfFirst { mediaTrack ->
                        mediaTrack.label == caption.language
                    }.let { defaultIndexCaptionTrack ->
                        if (defaultIndexCaptionTrack >= 0) {
                            player?.changeTrack(textTracks[defaultIndexCaptionTrack].uniqueId)
                            controlsView.setCaptionOnSelector(defaultIndexCaptionTrack)
                            return
                        }
                    }
                }
            }
        }
    }

    override fun setTrack(track: MediaTrack) {
        changeTrack(track.uniqueId)
    }

    fun changeTrack(id: String) {
        player?.changeTrack(id)
        tracks?.filter { it.type == TrackType.TEXT }?.indexOfFirst { mediaTrack ->
            mediaTrack.uniqueId == id
        }?.let { trackIndex ->
            controlsView.setCaptionOnSelector(trackIndex)
        }
    }

    fun getTracks() : List<MediaTrack>? {
        return tracks?.toList()
    }

    private fun setUpNetwork(){
        val gson: Gson = GsonBuilder().create()
        val client: OkHttpClient = OkHttpClient.Builder().build()
        retroFitInstance = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl("https://open.http.mp.streamamg.com")
            .client(client)
            .build()

        isLiveAPI = retroFitInstance.create(PlayKitIsLiveAPI::class.java)
        contextDataAPI = retroFitInstance.create(PlayKitContextDataAPI::class.java)
        tracksAPI = retroFitInstance.create(PlayKitTracksAPI::class.java)
    }

    fun castingURL(format: AMGMediaFormat = AMGMediaFormat.HLS, completion: (URL?) -> Unit) {
        castingCompletion = completion
        castingURL = null
        currentMediaItem?.let {mediaItem ->
            when (format){
                AMGMediaFormat.HLS -> {
                    val asset = URL("${mediaItem.serverURL}/p/${mediaItem.partnerID}/sp/0/playManifest/entryId/${mediaItem.entryID}/format/applehttp/${validKS(mediaItem.ks)}protocol/https/manifest.m3u8")
                    if (!mediaItem.serverURL.isNullOrEmpty() && !asset.toString().isNullOrEmpty()) {
                        getCastingURL(asset.toString())
                    } else {
                        completion(null)
                    }
                }
                AMGMediaFormat.MP4 -> {
                    val asset = URL("${mediaItem.serverURL}/p/${mediaItem.partnerID}/sp/0/playManifest/entryId/${mediaItem.entryID}/format/url/${validKS(mediaItem.ks)}protocol/https/video/mp4")
                    if (!mediaItem.serverURL.isNullOrEmpty() && !asset.toString().isNullOrEmpty()) {
                        getCastingURL(asset.toString())
                    } else {
                        completion(null)
                    }
                }
            }
         }
    }

    fun castingURL(server: String, partnerID: Int, entryID: String, ks: String? = null, format: AMGMediaFormat = AMGMediaFormat.HLS, completion: (URL?) -> Unit) {
        castingCompletion = completion
        castingURL = null
        when (format){
            AMGMediaFormat.HLS -> {
                val asset = URL("$server/p/$partnerID/sp/0/playManifest/entryId/$entryID/format/applehttp/${validKS(ks)}protocol/https/manifest.m3u8")
                if (asset.toString().isNotEmpty()) {
                    getCastingURL(asset.toString())
                } else {
                    completion(null)
                }
            }
            AMGMediaFormat.MP4 -> {
                val asset = URL("$server/p/$partnerID/sp/0/playManifest/entryId/$entryID/format/url/${validKS(ks)}protocol/https/video/mp4")
                if (asset.toString().isNotEmpty()) {
                    getCastingURL(asset.toString())
                } else {
                    completion(null)
                }
            }
        }
    }

    fun currentTime(): Long {
        return player?.currentPosition ?: 0
    }

    internal fun validKS(ks: String?, trailing: Boolean = false): String {
        if (ks != null && ks.isNotEmpty()) {
            if (trailing) {
                return "ks=$ks&"
            }
            return "ks/$ks/"
        }
        return ""
    }

    fun sendCastingURL(url: String?) {
        if (url.isNullOrEmpty() || URL(url).toString().isNullOrEmpty()) {
            return
        }
        if (castingURL == null) {
            castingURL = URL(url)
            castingCompletion?.invoke(castingURL)
        }
    }

    fun getCastingURL(url: String) {
        val validURL = URL(url.replace(" ", "%20"))
        if (validURL == null || validURL.toString().isNullOrEmpty()) {
            return
        }
        initialCastingURL = validURL.toString()

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        thread {
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(this@AMGPlayKit.javaClass.simpleName, e.localizedMessage)
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.request.url.toString()
                    sendCastingURL(responseBody)
                }
            })
        }
    }

    fun getCurrentPlayerAssett(){
        (player as PlayerController).let {playerController ->

        }
    }

    fun setPlayKitListener(listener: AMGPlayKitListener) {
        this.listener = listener
    }

    fun playerResume() {
        player?.onApplicationResumed()
    }

    fun playerPause() {
        player?.onApplicationPaused()
    }

    private fun setCurrentPlayhead() {
        player?.let {
            Handler(Looper.getMainLooper()).post {
                updatePlayheadPosition(it.currentPosition)
            }
        }
    }

    private fun setUpStandardControls() {
        if (!usingStandardControls) {
            controlsView.visibility = GONE
        } else {
            control = controlsView
            controlsView.player = this
            controlsView.visibility = VISIBLE
        }
        hideControls()
    }

    private fun playEventOccurred() {
        checkForDestroy()
        currentPlayerState = AMGPlayerState.Playing
        control?.play()
    }

    private fun stopEventOccurred() {
        checkForDestroy()
        currentPlayerState = AMGPlayerState.Stopped
        control?.pause()
    }

    private fun checkForDestroy(){
        if (playerShouldDestroy){
            destroyPlayer()
        }
    }

    private fun changeDuration(length: Long) {
        control?.changeMediaLength(length)
    }

    private fun updatePlayheadPosition(position: Long) {
        control?.changePlayHead(position)
        currentPlayHeadPosition = position
    }

    private fun errorOccurred(error: PlayerEvent.Error?) {
        error?.let {

        }
    }

    private fun setUpAttributes(context: Context, attributeSet: AttributeSet) {
        val array = context.obtainStyledAttributes(attributeSet, R.styleable.AMGPlayKit, 0, 0)
        val partnerID = array.getInteger(R.styleable.AMGPlayKit_partner_id, 0)
        partnerId = partnerID
        usingStandardControls = array.getBoolean(R.styleable.AMGPlayKit_use_standard_controls, false)
    }

    /**
    Changes the URL of the analytics endpoint

    Should only be used if targetting a secondary or non-standard analytics server

    - Parameter url: The URL of the server to target
     */
    fun setAnalyticsURL(url: String) {
        analyticsURL = url
    }

    /**
    Set custom header for the Analytics API.

    Should only be used if targetting a secondary or non-standard analytics server

    - Parameter requestHeader: Dictionary of header keys and value.
     */
    fun setAnalyticsCustomHeader(requestHeader: Map<String, String>?) {
        analyticsHeaders = requestHeader ?: mapOf()
    }

    /**
    Set the request method if POST or GET.

    Should only be used if targetting a secondary or non-standard analytics server

    - Parameter requestMethod: AMGRequestMethod can be type POST or GET.
     */
    fun setAnalyticsRequestMethod(requestMethod: AMGRequestMethod?) {
        analyticsMethod = requestMethod ?: AMGRequestMethod.GET
    }

    /**
    Configures the player to use the baked in standard controls, using an optional configuration file

    - Parameter config: A configuration model created by either deserializing a JSON file, or by creation with the AMGControlBuilder builder class
     */
    fun addStandardControl(config: AMGPlayKitStandardControlsConfigurationModel? = null) {
        var controlConfig = config
        if (controlConfig == null) {
            controlConfig = AMGPlayKitStandardControlsConfigurationModel()
        }
        usingStandardControls = true
        setUpStandardControls()
        controlsView.configureView(controlConfig)
        controlVisibleDuration = controlConfig.fadeOutAfter
        skipForwardTime = controlConfig.skipForwardTime
        skipBackwardTime = controlConfig.skipBackwardTime
        bitrateSelector = controlConfig.bitrateSelector
        subtitleSelector = controlConfig.subtitleSelector
    }

    /**
    Adds the Partner ID to the instance of AMGPlayKit.

    Should be used if instantiating the view via Storyboard, or if the view was instantiated manually without the Partner ID

    - Parameter partnerID: An integer value representing the Partner ID to be used in any played media
     */
    fun addPartnerID(partnerId: Int) {
        this.partnerId = partnerId
    }

//    fun isLive(): Boolean {
//        player?.let { player ->
//            return player.isLive
//        }
//        return false
//    }

    internal fun loadMedia(mediaConfig: MediaItem, mediaType: AMGMediaType = AMGMediaType.VOD, startPosition: Long = -1, bitrate: FlavorAsset? = null) {
        currentMediaItem = mediaConfig
        currentMediaType = mediaType
        if (player == null) {
            return
        }

        if (startPosition >= 0) {
            mediaConfig.mediaConfig.startPosition = startPosition / 1000
        }

        updateAnalyticsPlugin(mediaConfig.entryID)
        player?.prepare(mediaConfig.mediaConfig)
        updateBitrateSelector { bitrates ->
            if (listBitrate.orEmpty() != bitrates) {
                listener?.bitrateChangeOccurred(bitrates)
            }
            listBitrate = bitrates
        }

        controlsView.setMediaType(mediaType)
        if (mediaType == AMGMediaType.VOD){
            isLive(mediaConfig.serverURL, mediaConfig.entryID, mediaConfig.ks){isLiveBool ->
                if (isLiveBool){
                    controlsView.setMediaType(AMGMediaType.Live)
                    controlsView.setIsLive()
                }
            }
        } else {
            controlsView.setMediaType(AMGMediaType.Live)
            controlsView.setIsLive()
        }
        if (bitrate?.bitrate != null) {
            player?.settings?.setABRSettings(ABRSettings().setMaxVideoBitrate(bitrate.bitrate * 1024))
        }
        player?.play()
    }

    fun loadMedia(serverUrl: String, entryID: String, ks: String? = null, title: String? = null, mediaType: AMGMediaType = AMGMediaType.VOD, startPosition: Long = -1) {
        if (partnerId > 0) {
            fetchTracksData(serverUrl, entryID, partnerId, ks) {
                if (startPosition >= 0) {
                    loadMedia(MediaItem(serverUrl, partnerId, entryID, ks, title, mediaType, it), mediaType, startPosition)
                } else {
                    loadMedia(MediaItem(serverUrl, partnerId, entryID, ks, title, mediaType, it), mediaType)
                }
            }
        } else {
            print("Please provide a PartnerID with the request, add a default with 'addPartnerID(partnerID:Int)' or set a default in the initialiser")
        }
    }

    fun loadMedia(serverUrl: String, partnerID: Int, entryID: String, ks: String? = null, title: String? = null, mediaType: AMGMediaType = AMGMediaType.VOD) {
        partnerId = partnerID
        fetchTracksData(serverUrl, entryID, partnerID, ks) {
            loadMedia(MediaItem(serverUrl, partnerID, entryID, ks, title, mediaType, it), mediaType)
        }
    }


    fun serveAdvert(adTagURL: String) {
        player?.updatePluginConfig(IMAPlugin.factory.name, getIMAPluginConfig(adTagURL))
    }

    private fun updateAnalyticsPlugin(entryID: String) {
        if (analyticsConfiguration.analyticsService == AMGAnalyticsService.AMGANALYTICS) {
            val kavaConfig = AMGAnalyticsPluginConfig()
                .setPartnerId(partnerId)
                .setUiConfId(analyticsConfiguration.configID)
                .setUserLocation(analyticsConfiguration.userLocation)
                .setBaseUrl(analyticsURL)
                .setMethodRequest(analyticsMethod)
                .setHeaders(analyticsHeaders)
                .setEntryId(entryID)
            player?.updatePluginConfig(AMGAnalyticsPlugin.factory.name, kavaConfig)
        }

        if (analyticsConfiguration.analyticsService == AMGAnalyticsService.YOUBORA) {
            val youboraConfig = youboraPluginConfig()
            player?.updatePluginConfig(YouboraPlugin.factory.name, youboraConfig)
        }
    }

    private fun createPlugins(context: Context): PKPluginConfigs {

        var pluginConfigs = PKPluginConfigs()
        when (analyticsConfiguration.analyticsService) {
            AMGAnalyticsService.AMGANALYTICS -> {
                PlayKitManager.registerPlugins(context, AMGAnalyticsPlugin.factory)
                var kavaConfig = AMGAnalyticsPluginConfig()
                    .setPartnerId(analyticsConfiguration.partnerID)
                    .setUiConfId(analyticsConfiguration.configID)
                    .setUserLocation(analyticsConfiguration.userLocation)
                    .setBaseUrl(analyticsURL)
                    .setMethodRequest(analyticsMethod)
                    .setHeaders(analyticsHeaders)
                pluginConfigs.setPluginConfig(AMGAnalyticsPlugin.factory.name, kavaConfig)
            }
            AMGAnalyticsService.YOUBORA -> {
                PlayKitManager.registerPlugins(context, YouboraPlugin.factory)
                val youboraConfig = youboraPluginConfig()
                pluginConfigs.setPluginConfig(YouboraPlugin.factory.name, youboraConfig)
            }
            else -> {}
        }

        PlayKitManager.registerPlugins(context, IMAPlugin.factory)

         //, IMAPlugin.factory

        pluginConfigs.setPluginConfig(IMAPlugin.factory.name, getIMAPluginConfig(""))
        return pluginConfigs
    }

    private fun youboraPluginConfig(): JsonObject {
        val youboraConfig = JsonObject()
        youboraConfig.addProperty("accountCode", analyticsConfiguration.accountCode)
        analyticsConfiguration.userName?.let {name ->
            youboraConfig.addProperty("username", name)
        }

        if (analyticsConfiguration.youboraParameters.isNotEmpty()) {
            youboraConfig.add("extraParams", youboraParameters(analyticsConfiguration.youboraParameters))
        }
        return youboraConfig
    }

    private fun youboraParameters(params: ArrayList<YouboraParameter>): JsonObject {
        val parametersConfig = JsonObject()
        for (param in params){
            parametersConfig.addProperty("param${param.id}", param.value)
        }
        return parametersConfig
    }

    fun updateYouboraParameter(id: Int, value: String){
        analyticsConfiguration.updateYouboraParameter(id, value)
    }

    private fun getIMAPluginConfig(adTagUrl: String): IMAConfig? {
        val videoMimeTypes: MutableList<String> = ArrayList()
        videoMimeTypes.add(PKMediaFormat.mp4.mimeType)
        videoMimeTypes.add(PKMediaFormat.hls.mimeType)
        return IMAConfig().setAdTagUrl(adTagUrl).enableDebugMode(false).setVideoMimeTypes(videoMimeTypes)
    }

    // Player / Control Interface


    /**
    Manually play the queued media track
     */
    override fun play() {
        player?.let {

            when (currentPlayerState) {
                AMGPlayerState.Error,
                AMGPlayerState.Idle -> {
                    currentMediaItem?.let { item ->
                        loadMedia(item, currentMediaType, currentPlayHeadPosition)
                    }
                }
                else -> {
                    if (it.currentPosition >= it.duration) {
                        it.replay()
                    } else {
                        it.play()
                    }
                }
            }


        }
        startControlVisibilityTimer()
    }

    /**
    Manually pause the queued media track
     */
    override fun pause() {
        player?.pause()
        control?.pause()
        startControlVisibilityTimer()
    }

    /**
    Manually set the playhead for the queued media track
     */
    override fun scrub(position: Long) {
        player?.seekTo(position)
    }

    override fun skipBackward() {
        player?.let {
            val time = it.currentPosition - skipBackwardTime
            if (time < 0) {
                it.seekTo(0)
            } else {
                it.seekTo(time)
            }
        }
    }

    override fun skipForward() {
        player?.let {
            val time = it.currentPosition + skipForwardTime
            if (time > it.duration) {
                it.seekTo(it.duration)
            } else {
                it.seekTo(time)
            }
        }
    }

    override fun goLive() {
        player?.seekTo(Long.MAX_VALUE)
    }

    /**
    Set the time skipped for forward skip
     */
    public fun skipForwardTime(duration: Long) {
        skipForwardTime = duration
    }

    /**
    Set the time skipped for backward skip
     */
    public fun skipBackwardTime(duration: Long) {
        skipBackwardTime = duration
    }

    /**
    Set the time skipped for backward and forward skip
     */
    public fun skipTime(duration: Long) {
        skipBackwardTime = duration
        skipForwardTime = duration
    }

    /**
    Set the control interface for the current UI Control class
     */
    override fun setControlInterface(controls: AMGControlInterface) {
        control = controls
    }

    /**
    Manually halt the fade out timer for overlayed UI controls - the timer will not restart until another action that starts it occurs
     */
    override fun cancelTimer() {
        if (controlVisibleTimer != null) {
            controlVisibleTimer?.cancel()
            controlVisibleTimer = null
        }
    }

    /**
    Manually change the highest bitrate for the rest of the stream
     */
    override fun setMaximumBitrate(bitrate: FlavorAsset?) {
        bitrate.let {
            setBitrate(it)
        }
        startControlVisibilityTimer()
    }

    fun setBitrateAuto() {
        setMaximumBitrate(listBitrate?.lastOrNull())
    }

    @Deprecated("Use the new setMaximumBitrate(bitrate: FlavorAsset?) method", ReplaceWith(
        "setMaxVideoBitrate(bitRate))",
        ""))
    fun setMaximumBitrate(bitRate: Long){
        player?.settings?.setABRSettings(ABRSettings().setMaxVideoBitrate(bitRate))
    }

    private fun bringControlsToForeground() {
        if (usingStandardControls) {
            Handler(Looper.getMainLooper()).post {
                controlsView.showControls(true)
            }
            startControlVisibilityTimer()
        }
    }

    /**
    Manually start the fade out timer for overlayed UI controls
     */
    override fun startControlVisibilityTimer() {
        cancelTimer()
        controlVisibleTimer = Timer()
        controlVisibleTimer?.schedule(object : TimerTask() {
            override fun run() {
                hideControls()
            }
        }, controlVisibleDuration)

        //Timer.scheduledTimer(timeInterval: controlVisibleDuration, target: self, selector: #selector(hideControls), userInfo: nil, repeats: false)
    }

    override fun playState(): AMGPlayKitPlayState {
        player?.let { amgPlayer ->
            return when (player?.isPlaying) {
                true -> AMGPlayKitPlayState.playing
                else -> AMGPlayKitPlayState.paused
            }
        }
        return AMGPlayKitPlayState.idle
    }

    private fun createMedia(entryID: String, url: String, title: String?): PKMediaEntry{
        val media = PKMediaEntry()
        media.id = entryID
        if (title != null) {
            media.name = title
        }
        var sources = PKMediaSource()
        sources.id = entryID
        sources.url = url
        sources.mediaFormat = PKMediaFormat.hls;
        media.sources = listOf(sources)
        media.mediaType = PKMediaEntry.MediaEntryType.Vod
        return media
    }

    fun loadLocalMedia(entryID: String, url: String, title: String?){
        updateAnalyticsPlugin(entryID)
        var mediaConfig = PKMediaConfig()
        mediaConfig.mediaEntry = createMedia(entryID,url,title)
        player?.prepare(mediaConfig)
        controlsView.setMediaType(AMGMediaType.VOD)
        controlsView.setIsVOD()
        player?.play()
    }


    private fun hideControls() {
        print("Hiding controls")

        Handler(Looper.getMainLooper()).post {
            controlsView.showControls(false)
        }
    }

    /**
    Define the status of the 'is live' badge - the badge must be added via either of the 'setIsLiveImage' functions previous to this happening

    - Parameter shouldShow: Boolean defining if the image should be displayed
     */
    fun setiSliveImageShowing(shouldShow: Boolean) {
        isLiveImageView.visibility = when (shouldShow) {
            true -> VISIBLE
            false -> GONE
        }
    }

    /**
    Define the status of the 'logo' badge - the badge must be added via either of the 'setLogoImage' functions previous to this happening

    - Parameter shouldShow: Boolean defining if the image should be displayed
     */
    fun setlogoImageShowing(shouldShow: Boolean) {
        logoImageView.visibility = when (shouldShow) {
            true -> VISIBLE
            false -> GONE
        }
    }

    /**
    Sets the 'is live' image for display int he top left corner of the player using a named asset from the app's asset catalogue

    - Parameter drawable: Local resource ID (as an Int) of the image to be used
    - Parameter atWidth: Pixel width of the desired display size - will be translated to DIP by the AMG Play Kit
     */
    public fun setIsLiveImage(drawable: Int, atWidth: Int = 70) {
        isLiveImageView.setImageResource(drawable)
        isLiveImageView.setWidthToBe(atWidth)
    }

    /**
    Sets the 'is live' image for display int he top left corner of the player by defining the URL (as a string)

    - Parameter url: URL as a String that contains the image to be downloaded and displayed
    - Parameter atWidth: Pixel width of the desired display size - will be translated to DIP by the AMG Play Kit
    - Parameter atHeight: Pixel height of the desired display size - will be translated to DIP by the AMG Play Kit
     */
    public fun setIsLiveImage(url: String, atWidth: Int, atHeight: Int) {
        Picasso.get().load(url).resize(atWidth.dpToPixels(context), atHeight.dpToPixels(context)).transform(CustomImageTransform(atWidth)).into(isLiveImageView)
    }

    /**
    Sets the 'logo' image for display int he top right corner of the player using a named asset from the app's asset catalogue

    - Parameter drawable: Local resource ID (as an Int) of the image to be used
    - Parameter atWidth: Pixel width of the desired display size - will be translated to DIP by the AMG Play Kit
     */
    public fun setlogoImage(drawable: Int, atWidth: Int = 70) {
        logoImageView.setImageResource(drawable)
        logoImageView.setWidthToBe(atWidth)
    }

    /**
    Sets the 'logo' image for display int the top right corner of the player by defining the URL (as a string)

    - Parameter url: URL as a String that contains the image to be downloaded and displayed
    - Parameter atWidth: Pixel width of the desired display size - will be translated to DIP by the AMG Play Kit
    - Parameter atHeight: Pixel height of the desired display size - will be translated to DIP by the AMG Play Kit

     */
    public fun setlogoImage(url: String, atWidth: Int, atHeight: Int) {
        Picasso.get().load(url).resize(atWidth.dpToPixels(context), atHeight.dpToPixels(context)).into(logoImageView)
    }

    fun initialiseSensor(activity: Activity, enable: Boolean) {
        orientationActivity = activity
        mSensorStateChanges = SensorStateChangeActions.SWITCH_FROM_POTRAIT_TO_STANDARD
        if (enable) {
            sensorEvent = object : OrientationEventListener(
                    activity,
                    SensorManager.SENSOR_DELAY_NORMAL
            ) {
                override fun onOrientationChanged(orientation: Int) {
                    if (null != mSensorStateChanges && mSensorStateChanges == SensorStateChangeActions.WATCH_FOR_LANDSCAPE_CHANGES && (orientation >= 60 && orientation <= 120 || orientation >= 240 && orientation <= 300)) {
                        mSensorStateChanges = SensorStateChangeActions.SWITCH_FROM_LANDSCAPE_TO_STANDARD
                    } else if (null != mSensorStateChanges && mSensorStateChanges == SensorStateChangeActions.SWITCH_FROM_LANDSCAPE_TO_STANDARD && (orientation <= 40 || orientation >= 320)) {
                        orientationActivity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        mSensorStateChanges = null
                        mSensorStateChanges = SensorStateChangeActions.WATCH_FOR_PORTAIT_CHANGES
                    } else if (null != mSensorStateChanges && mSensorStateChanges == SensorStateChangeActions.WATCH_FOR_PORTAIT_CHANGES && (orientation >= 300 && orientation <= 359 || orientation >= 0 && orientation <= 45)) {
                        mSensorStateChanges = SensorStateChangeActions.SWITCH_FROM_POTRAIT_TO_STANDARD
                    } else if (null != mSensorStateChanges && mSensorStateChanges == SensorStateChangeActions.SWITCH_FROM_POTRAIT_TO_STANDARD && (orientation <= 300 && orientation >= 240 || orientation <= 130 && orientation >= 60)) {
                        orientationActivity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        mSensorStateChanges = null
                        mSensorStateChanges = SensorStateChangeActions.WATCH_FOR_LANDSCAPE_CHANGES
                    }
                }
            }
            sensorEvent?.enable()
        } else {
            sensorEvent?.disable()
            sensorEvent = null
        }
    }

    fun fullScreen() {
        sensorEvent?.let { orientationSensor ->
            orientationActivity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            mSensorStateChanges = SensorStateChangeActions.WATCH_FOR_LANDSCAPE_CHANGES
            orientationSensor.enable()
            isFullScreen = true
            controlsView.hideFullScreenButton(0)
        }
    }

    fun minimise() {
        sensorEvent?.let { orientationSensor ->
            orientationActivity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            mSensorStateChanges = SensorStateChangeActions.WATCH_FOR_PORTAIT_CHANGES
            orientationSensor.enable()
            isFullScreen = false
            controlsView.hideFullScreenButton(1)
        }
    }

    override fun swapOrientation() {
        if (isFullScreen) {
            minimise()
        } else {
            fullScreen()
        }
    }

    fun setSpoilerFree(enabled: Boolean){
        controlsView.setSpoilerFree(enabled)
    }

    fun updateMediaType(mediaType: AMGMediaType) {
        currentMediaType = mediaType
        controlsView.setMediaType(mediaType)
    }
}

fun Int.dpToPixels(context: Context): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics
).toInt()

fun ImageView.setWidthToBe(width: Int) {
    val myParams = this.layoutParams
    myParams.width = width.dpToPixels(context)
//    val params = ConstraintLayout.LayoutParams(
//        width.dpToPixels(context),
//        ConstraintLayout.LayoutParams.WRAP_CONTENT
//    )
    this.layoutParams = myParams
}
