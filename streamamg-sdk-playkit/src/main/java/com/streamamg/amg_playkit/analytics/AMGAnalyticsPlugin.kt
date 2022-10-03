package com.streamamg.amg_playkit.analytics

/*

Custom AMG Analytics plugin based on the standard KavaAnalyticsPlugin bundle
Created to give customisable URLs for specific AMG parameters

 */

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.kaltura.android.exoplayer2.C
import com.kaltura.netkit.connect.executor.APIOkRequestsExecutor
import com.kaltura.netkit.connect.executor.RequestQueue
import com.kaltura.netkit.utils.OnRequestCompletion
import com.kaltura.playkit.*
import com.kaltura.playkit.PKMediaEntry.MediaEntryType
import com.kaltura.playkit.PlayerEvent.*
import com.kaltura.playkit.player.PKPlayerErrorType
import com.kaltura.playkit.player.metadata.PKTextInformationFrame
import com.kaltura.playkit.plugin.kava.BuildConfig
import com.kaltura.playkit.plugins.kava.KavaAnalyticsConfig
import com.kaltura.playkit.plugins.kava.KavaAnalyticsEvent
import com.kaltura.playkit.plugins.kava.KavaAnalyticsPlugin
import com.kaltura.playkit.plugins.kava.KavaEvents
import com.kaltura.playkit.utils.Consts
import com.streamamg.amg_playkit.constants.AMGRequestMethod
import org.json.JSONException
import org.json.JSONObject
import java.text.DecimalFormat

class AMGAnalyticsPlugin : PKPlugin() {

    private val log = PKLog.get(KavaAnalyticsPlugin::class.java.simpleName)
    private val TEXT = "TEXT"

    private var player: Player? = null
    private var messageBus: MessageBus? = null
    private var playerState: PlayerState? = null
    private var mediaConfig: PKMediaConfig? = null
    private var dataHandler: AMGDataHandler? = null
    private var requestExecutor: RequestQueue? = null
    private var pluginConfig: AMGAnalyticsPluginConfig? = null

    private var playheadUpdated: PlayheadUpdated? = null
    private var playReached25 = false
    private var playReached50 = false
    private var playReached75 = false
    private var playReached100 = false

    private var isAutoPlay = false
    private var isImpressionSent = false
    private var isBufferingStart = false
    private var isEnded = false
    private var isPaused = true
    private var isFirstPlay: Boolean? = null
    private var isFatalError = false
    private var isLiveMedia = false

    private var viewTimer: AMGViewTimer? = null
    private val viewEventTrigger = initViewTrigger()
    private var applicationBackgroundTimeStamp: Long = 0
    private var decimalFormat: DecimalFormat? = null
    private var startPlayTime: Long = 0
    private var loadingTime: Long = 0

    private var currentLoadStatus = 0


    companion object {
        val factory: Factory = object : Factory {
            override fun getName(): String {
                return "kava"
            }

            override fun newInstance(): PKPlugin {
                return AMGAnalyticsPlugin()
            }

            override fun getVersion(): String {
                return BuildConfig.VERSION_NAME
            }

            override fun warmUp(context: Context) {}
        }
    }

    override fun onLoad(player: Player?, config: Any?, messageBus: MessageBus?, context: Context) {
        log.d("onLoad")
        decimalFormat = DecimalFormat("#")
        decimalFormat!!.maximumFractionDigits = 3
        this.player = player
        this.messageBus = messageBus
        requestExecutor = APIOkRequestsExecutor.getSingleton()
        addListeners()
        dataHandler = AMGDataHandler(context, player)
        onUpdateConfig(config)
    }

    private fun addListeners() {
        messageBus!!.addListener(this, stateChanged) { event: StateChanged -> handleStateChanged(event) }
        messageBus!!.addListener(this, canPlay) { event: PKEvent? ->
            isLiveMedia = player!!.isLive
            if (isFirstPlay == null || isFirstPlay!!) {
                dataHandler?.handleCanPlay()
            }
        }
        messageBus!!.addListener(this, loadedMetadata) { event: PKEvent? ->
            currentLoadStatus = 0
            startPlayTime = System.currentTimeMillis()
            if (!isImpressionSent) {
                sendAnalyticsEvent(KavaEvents.IMPRESSION)
                dataHandler!!.handleLoadedMetaData()
                if (isAutoPlay) {
                    sendAnalyticsEvent(KavaEvents.PLAY_REQUEST)
                    isAutoPlay = false
                }
                isImpressionSent = true
            }
        }
        messageBus!!.addListener(this, play) { event: PKEvent? ->
            if (isFirstPlay == null) {
                dataHandler!!.handleFirstPlay()
            }
            if (isImpressionSent && (isFirstPlay == null || !isPaused)) {
                sendAnalyticsEvent(KavaEvents.PLAY_REQUEST, 2)
            } else {
                isAutoPlay = true
            }
            if (isFirstPlay == null) {
                isFirstPlay = true
            }
        }
        messageBus!!.addListener(this, pause) {
            setIsPaused(true)

            sendAnalyticsEvent(KavaEvents.PAUSE, 3)
        }
        messageBus!!.addListener(this, playbackRateChanged) { event: PlaybackRateChanged? ->
            event?.let {
                dataHandler!!.handlePlaybackSpeed(it)
                sendAnalyticsEvent(KavaEvents.SPEED)
            }

        }
        messageBus!!.addListener(this, playing) {
            currentLoadStatus = 1
            loadingTime = System.currentTimeMillis() - startPlayTime
            if (isFirstPlay == null || isFirstPlay!!) {
                isFirstPlay = false
                sendAnalyticsEvent(KavaEvents.PLAY, 1)
                sendAnalyticsEvent(KavaEvents.VIEW)
                startViewTimer()
            } else {
                if (isPaused && !isEnded) {
                    sendAnalyticsEvent(KavaEvents.RESUME, 2)
                }
            }
            isEnded = false // needed in order to prevent sending RESUME event after REPLAY.
            setIsPaused(false)
        }
        messageBus!!.addListener(this, seeking) { event: Seeking? ->
            event?.let {
                val mediaEntryType = getMediaEntryType()
                if ((isFirstPlay == null || isFirstPlay!!) && (isLiveMedia || MediaEntryType.Live == mediaEntryType || MediaEntryType.DvrLive == mediaEntryType)) {
                    return@addListener
                }
                dataHandler!!.handleSeek(it)
                sendAnalyticsEvent(KavaEvents.SEEK, 9)
            }
        }

        messageBus!!.addListener(this, replay) { event: PKEvent? -> sendAnalyticsEvent(KavaEvents.REPLAY) }

        messageBus!!.addListener(this, sourceSelected) { event: SourceSelected? ->
            (event as PKEvent).let {
                dataHandler!!.handleSourceSelected(it)
            }
        }

        messageBus!!.addListener(this, ended) { event: PKEvent? ->
            val mediaType = getMediaEntryType()
            val isLive = isLiveMedia || mediaType == MediaEntryType.Live || mediaType == MediaEntryType.DvrLive
            if (!isLive) {
                maybeSentPlayerReachedEvent()
                if (!playReached100) {
                    playReached100 = true
                    sendAnalyticsEvent(KavaEvents.PLAY_REACHED_100_PERCENT, 8)
                        dataHandler?.handlePlayerReachedEnd()
                }
            }
            isEnded = true
            setIsPaused(true)
        }
        messageBus!!.addListener(this, playbackInfoUpdated) { event: PlaybackInfoUpdated? ->
            (event as PKEvent).let {
                if (dataHandler!!.handleTrackChange(event, Consts.TRACK_TYPE_VIDEO)) {
                    sendAnalyticsEvent(KavaEvents.FLAVOR_SWITCHED)
                }
            }
        }
        messageBus!!.addListener(this, tracksAvailable) { event: TracksAvailable? ->

            event?.let {
                dataHandler?.handleTracksAvailable(it)
            }
        }
        messageBus!!.addListener(this, videoTrackChanged) { event: VideoTrackChanged? ->

            (event as PKEvent).let {
                dataHandler?.handleTrackChange(it, Consts.TRACK_TYPE_VIDEO)
                sendAnalyticsEvent(KavaEvents.SOURCE_SELECTED)
            }
        }
        messageBus!!.addListener(this, audioTrackChanged) { event: AudioTrackChanged? ->
            (event as PKEvent).let {
                dataHandler!!.handleTrackChange(it, Consts.TRACK_TYPE_AUDIO)
                sendAnalyticsEvent(KavaEvents.AUDIO_SELECTED)
            }
        }
        messageBus!!.addListener(this, textTrackChanged) { event: TextTrackChanged? ->
            (event as PKEvent).let {
                dataHandler!!.handleTrackChange(it, Consts.TRACK_TYPE_TEXT)
                sendAnalyticsEvent(KavaEvents.CAPTIONS)
            }
        }
        messageBus!!.addListener(this, bytesLoaded) { event: BytesLoaded ->
            //log.d("bytesLoaded = " + event.trackType + " load time " + event.loadDuration);
            if (C.TRACK_TYPE_VIDEO == event.trackType || C.TRACK_TYPE_DEFAULT == event.trackType) {
                dataHandler!!.handleSegmentDownloadTime(event)
            } else if (C.TRACK_TYPE_UNKNOWN == event.trackType) {
                dataHandler!!.handleManifestDownloadTime(event)
            }
        }
        messageBus!!.addListener(this, metadataAvailable) { event: MetadataAvailable ->
            log.d("metadataAvailable = " + event.eventType())
            for (pkMetadata in event.metadataList) {
                if (pkMetadata is PKTextInformationFrame) {
                    val textFrame = pkMetadata
                    if (textFrame != null) {
                        if (TEXT == textFrame.id) {
                            try {
                                if (textFrame.value != null) {
                                    val textFrameValue = JSONObject(textFrame.value)
                                    val flavorParamsId = textFrameValue.getString("sequenceId")
                                    //log.d("metadataAvailable Received user text: flavorParamsId = " + flavorParamsId);
                                    dataHandler!!.handleSequenceId(flavorParamsId) //flavorParamsId = sequenceId from {"timestamp":1573049629312,"sequenceId":"32"}
                                }
                            } catch (e: JSONException) {
                                //e.printStackTrace();
                                log.e("Failed to parse the sequenceId from TEXT ID3 frame")
                                return@addListener
                            }
                        }
                    }
                }
            }
        }
        messageBus!!.addListener(this, error) { event: Error ->
            val error = event.error
            Log.d("AMG", "Error occurred in Analytics Plugin")

            if (event.error.errorType is PKPlayerErrorType) {
                currentLoadStatus = when (event.error.errorType as PKPlayerErrorType) {
                    PKPlayerErrorType.SOURCE_ERROR,
                    PKPlayerErrorType.RENDERER_ERROR,
                    PKPlayerErrorType.UNEXPECTED,
                    PKPlayerErrorType.SOURCE_SELECTION_FAILED,
                    PKPlayerErrorType.FAILED_TO_INITIALIZE_PLAYER,
                    PKPlayerErrorType.TRACK_SELECTION_FAILED,
                    PKPlayerErrorType.LOAD_ERROR,
                    PKPlayerErrorType.OUT_OF_MEMORY,
                    PKPlayerErrorType.REMOTE_COMPONENT_ERROR -> 2
                    PKPlayerErrorType.DRM_ERROR -> 3
                    PKPlayerErrorType.TIMEOUT -> 4
                    else -> 2
                }
            }

            if (error != null && !error.isFatal) {
                log.v("Error eventType = " + error.errorType + " severity = " + error.severity + " errorMessage = " + error.message)
                return@addListener
            }
            dataHandler!!.handleError(event, isFirstPlay, player!!.currentPosition)
            sendAnalyticsEvent(KavaEvents.ERROR)
            if (viewTimer != null) {
                viewTimer!!.setViewEventTrigger(null)
                viewTimer!!.stop()
            }
        }
        messageBus!!.addListener(this, PlayerEvent.playheadUpdated) { event: PlayheadUpdated? ->
            playheadUpdated = event
            //log.d("playheadUpdated event  position = " + playheadUpdated.position + " duration = " + playheadUpdated.duration);
            val mediaType = getMediaEntryType()
            val isLive = isLiveMedia || mediaType == MediaEntryType.Live || mediaType == MediaEntryType.DvrLive
            event?.let{
                dataHandler?.updateHeatMap(it)
            }
            if (!isLive) {
                maybeSentPlayerReachedEvent()
            }
        }

        messageBus!!.addListener(this, connectionAcquired) { event: ConnectionAcquired? ->
            event?.let {
                dataHandler!!.handleConnectionAcquired(it)
            }
        }
    }

    private fun getMediaEntryType(): MediaEntryType {
        var mediaType = MediaEntryType.Unknown
        if (mediaConfig != null && mediaConfig!!.mediaEntry != null) {
            mediaType = mediaConfig!!.mediaEntry!!.mediaType
        }
        return mediaType
    }

    override fun onUpdateMedia(mediaConfig: PKMediaConfig?) {
        log.d("onUpdateMedia")
        mediaConfig?.let {
            this.mediaConfig = it
            isLiveMedia = false
            clearViewTimer()
            pluginConfig?.let { plugin ->
                dataHandler?.onUpdateMedia(it, plugin)
            }
            resetFlags()
            viewTimer = AMGViewTimer()
            viewTimer!!.setViewEventTrigger(viewEventTrigger)
        }

    }

    override fun onUpdateConfig(config: Any?) {
        parsePluginConfig(config)?.let { pluginConfig ->
            this.pluginConfig = pluginConfig
            dataHandler?.onUpdateConfig(pluginConfig)
        }

    }

    override fun onApplicationPaused() {
        log.d("onApplicationPaused")
        applicationBackgroundTimeStamp = System.currentTimeMillis()
        if (dataHandler != null) {
            val mediaEntryType = getMediaEntryType()
            dataHandler!!.onApplicationPaused(mediaEntryType)
        }
        if (viewTimer != null) {
            viewTimer!!.setViewEventTrigger(null)
            viewTimer!!.stop()
        }
    }

    override fun onApplicationResumed() {
        log.d("onApplicationResumed")
        val currentTimeInSeconds = System.currentTimeMillis() - applicationBackgroundTimeStamp
        if (dataHandler != null) {
            if (currentTimeInSeconds >= AMGStaticAnalyticsObjects.MAX_ALLOWED_VIEW_IDLE_TIME) {
                dataHandler!!.handleViewEventSessionClosed()
            }
            dataHandler!!.setOnApplicationResumed()
        }
        startViewTimer()
    }

    private fun startViewTimer() {
        if (viewTimer != null) {
            viewTimer!!.setViewEventTrigger(viewEventTrigger)
            viewTimer!!.start()
        }
    }

    override fun onDestroy() {
        if (messageBus != null) {
            messageBus!!.removeListeners(this)
        }
        clearViewTimer()
    }

    private fun clearViewTimer() {
        if (viewTimer != null) {
            viewTimer!!.setViewEventTrigger(null)
            viewTimer!!.stop()
            viewTimer = null
        }
    }

    private fun handleStateChanged(event: StateChanged) {
        when (event.newState) {
            PlayerState.BUFFERING -> {
                playerState = PlayerState.BUFFERING
                //We should start count buffering time only after IMPRESSION was sent.
                if (isImpressionSent) {
                    dataHandler!!.handleBufferingStart()
                    sendAnalyticsEvent(KavaEvents.BUFFER_START)
                    isBufferingStart = true
                }
            }
            PlayerState.READY -> {
                playerState = PlayerState.READY
                dataHandler!!.handleBufferingEnd()
                if (isBufferingStart) {
                    sendAnalyticsEvent(KavaEvents.BUFFER_END)
                    isBufferingStart = false
                }
            }
        }
    }

    private fun sendAnalyticsEvent(event: KavaEvents, eventID: Int = 0) {
        if (isInputInvalid()) return
        if (isFatalError) {
            return
        }
        if (event == KavaEvents.ERROR) {
            isFatalError = true
        }
        val params = dataHandler!!.collectData(
            event,
            mediaConfig!!.mediaEntry!!.mediaType,
            isLiveMedia,
            playheadUpdated
        )
        //val eid: String, val pid: Int, val dhm: String, val sid: String

        pluginConfig?.let { plugin ->
            params?.let { data ->

                // Analytics GET (default)
                var requestBuilder = AMGRequestBuilder.sendAnalyticsEvent(plugin.baseUrl, dataHandler?.getUserAgent(), data)

                if (plugin.methodRequest == AMGRequestMethod.POST) {
                    // Analytics POST
                    val request = AMGAnalyticsRequest(
                        data["eid"] ?: "",
                        data["pid"]?.toIntOrNull() ?: 0,
                        data["dhm"] ?: "",
                        data["sid"] ?: "")
                    data["rurl"]?.let { request.rurl = it }
                    data["den"]?.let { request.den = it.toLong() }
                    data["dpl"]?.let { request.dpl = it.toLong() }
                    data["dcn"]?.let { request.dcn = it.toLong() }
                    data["tsp"]?.let { request.tsp = it }
                    plugin.uiconfId?.let {
                        request.uci = it
                    }
                    request.vls = currentLoadStatus
                    request.vnt = eventID
                    request.uas = dataHandler?.getUserAgent() ?: ""
                    request.vlt = loadingTime

                    //Analytics POST
                    requestBuilder = AMGRequestBuilder.getRequest(plugin.baseUrl, dataHandler?.getUserAgent(), request)
                }

                plugin.headers?.let {
                    requestBuilder.build().headers.putAll(it)
                }

                requestBuilder.completion(OnRequestCompletion { response ->
                    log.d("onComplete: " + event.name)
                    try {
                        JsonParser.parseString(response.response)
                        // If no, exception thrown, we will treat response as Json format.
                        if (response == null || response.response == null) {
                            log.w("Kava event response is null")
                            return@OnRequestCompletion
                        }

                        val values = JSONObject(response.response)
                        val sid = values.optString("sid", "")
                        if (!sid.isNullOrEmpty()) {
                            dataHandler?.updateSessionID(sid)
                        }
                    } catch (e: JsonSyntaxException) {
                        // If exception thrown, we will treat response as String format.
                        val split = response.response.split(":")
                        if (split.count() == 2) {
                            dataHandler?.updateSessionID(split[1])
                        }
                    } catch (e: JSONException) {
                        // Legacy compatibility
                        if (response.response != null) {
                            dataHandler!!.setSessionStartTime(response.response)
                        }
                    }
                    messageBus!!.post(KavaAnalyticsEvent.KavaAnalyticsReport(event.name))
                })
     //       log.d("request sent " + event.name + " - " + requestBuilder.build().url)
                requestExecutor!!.queue(requestBuilder.build())
//                body.log()
//                body.toJson()
            }
        }
    }

//    private fun getRequest(url: String, body: AMGAnalyticsRequest): RequestBuilder {
//        val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), body.toJson())
//        //return Request.Builder().url(url).post(requestBody).build().
//            var requestBuilder = RequestBuilder().method(HTTP_METHOD_POST)
//            //.getHeaders().put("User-Agent", userAgent);
//    }

    private fun isInputInvalid(): Boolean {
        if (mediaConfig == null || mediaConfig!!.mediaEntry == null) {
            return true
        }
        if (!isValidEntryId()) {
            if (pluginConfig!!.partnerId == null && pluginConfig!!.entryId == null) {
                pluginConfig!!.partnerId = KavaAnalyticsConfig.DEFAULT_KAVA_PARTNER_ID
                pluginConfig!!.entryId = KavaAnalyticsConfig.DEFAULT_KAVA_ENTRY_ID
            } else {
                return true
            }
        }
        pluginConfig?.let {
            if (!it.isPartnerIdValid()) {
                log.w("Can not send analytics event. Mandatory field partnerId is missing")
                return true
            }
        }

        return false
    }

    private fun isValidEntryId(): Boolean {
        if (mediaConfig == null || mediaConfig!!.mediaEntry == null) {
            return false
        }
        var mediaEntryValid = true
        mediaEntryValid = if (mediaConfig!!.mediaEntry!!.id == null) {
            log.w("Can not send analytics event. Mandatory field entryId is missing")
            false
        } else {
            // for OTT assetId is not valid for Kava
            mediaEntryValid && !TextUtils.isDigitsOnly(mediaConfig!!.mediaEntry!!.id)
        }
        var metadataVaild = true
        if ((pluginConfig == null || TextUtils.isEmpty(pluginConfig!!.entryId)) && !isEntryIdInMetadata()) {
            log.w("Can not send analytics event. Mandatory field entryId is missing")
            metadataVaild = false
        }
        return mediaEntryValid || metadataVaild
    }

    private fun isEntryIdInMetadata(): Boolean {
        return mediaConfig != null && mediaConfig!!.mediaEntry != null && mediaConfig!!.mediaEntry!!.metadata != null &&
                mediaConfig!!.mediaEntry!!.metadata.containsKey("entryId") &&
                !TextUtils.isEmpty(mediaConfig!!.mediaEntry!!.metadata["entryId"])
    }

    private fun parsePluginConfig(config: Any?): AMGAnalyticsPluginConfig? {
        if (config is AMGAnalyticsPluginConfig) {
            return config
        } else if (config is JsonObject) {
            return Gson().fromJson(config as JsonObject?, AMGAnalyticsPluginConfig::class.java)
        }
        // If no config passed, create default one.
        return AMGAnalyticsPluginConfig()
    }

    private fun maybeSentPlayerReachedEvent() {
        var progress = 0f
        if (playheadUpdated != null && playheadUpdated!!.position >= 0 && playheadUpdated!!.duration > 0) {
            progress = playheadUpdated!!.position.toFloat() / playheadUpdated!!.duration
        }
        if (progress < 0.25) {
            return
        }
        if (!playReached25) {
            playReached25 = true
            sendAnalyticsEvent(KavaEvents.PLAY_REACHED_25_PERCENT)
        }
        if (!playReached50 && progress >= 0.5) {
            playReached50 = true
            sendAnalyticsEvent(KavaEvents.PLAY_REACHED_50_PERCENT)
        }
        if (!playReached75 && progress >= 0.75) {
            playReached75 = true
            sendAnalyticsEvent(KavaEvents.PLAY_REACHED_75_PERCENT)
        }
    }

    private fun setIsPaused(isPaused: Boolean) {
        this.isPaused = isPaused
        if (viewTimer != null) {
            if (isPaused) {
                viewTimer!!.pause()
            } else {
                viewTimer!!.resume()
            }
        }
    }

    private fun resetFlags() {
        setIsPaused(true)
        isEnded = false
        isFirstPlay = null
        isLiveMedia = false
        isFatalError = false
        isImpressionSent = false
        isBufferingStart = false
        playReached100 = false
        playReached75 = playReached100
        playReached50 = playReached75
        playReached25 = playReached50
        playheadUpdated = null
    }

    private fun initViewTrigger(): AMGViewTimer.ViewEventTrigger {
        return object : AMGViewTimer.ViewEventTrigger {
            override fun onTriggerViewEvent() {
                //When we send VIEW event, while player is buffering we should
                //manually update buffer time. So we will simulate handleBufferEnd()
                if (playerState == PlayerState.BUFFERING) {
                    dataHandler!!.handleBufferingEnd()
                }
                sendAnalyticsEvent(KavaEvents.VIEW)
            }

            override fun onResetViewEvent() {
                dataHandler!!.handleViewEventSessionClosed()
            }

            override fun onTick() {}
        }
    }

}