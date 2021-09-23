package com.streamamg.amg_playkit.analytics

import android.content.Context
import android.media.AudioManager
import com.kaltura.playkit.*
import com.kaltura.playkit.PKMediaEntry.MediaEntryType
import com.kaltura.playkit.PlayerEvent.*
import com.kaltura.playkit.ads.PKAdErrorType
import com.kaltura.playkit.player.PKPlayerErrorType
import com.kaltura.playkit.player.PlayerSettings
import com.kaltura.playkit.plugins.ads.AdEvent
import com.kaltura.playkit.plugins.kava.*
import com.kaltura.playkit.utils.Consts
import java.util.*

class AMGDataHandler(context: Context, player: Player?) {
    private val log = PKLog.get(AMGDataHandler::class.java.simpleName)

    private val KB_MULTIPLIER = 1024L

    private val PLAYER_ERROR_STR = "Player error occurred"

    private var context: Context? = null
    private var player: Player? = null

    private var errorCode = 0
    private var errorDetails: String? = null
    private var errorPosition: Int? = null
    private var eventIndex = 0
    private var totalBufferTimePerViewEvent = 0

    private var playTimeSum: Long = 0
    private var dvrThreshold: Long = 0
    private var actualBitrate: Long = 0
    private var currentPosition: Long = 0
    private var currentBufferPosition: Long = 0
    private var currentDuration: Long = 0
    private var joinTimeStartTimestamp: Long = 0
    private var canPlayTimestamp: Long = 0
    private var loadedMetaDataTimestamp: Long = 0
    private var totalBufferTimePerEntry: Long = 0
    private var lastKnownBufferingTimestamp: Long = 0
    private var targetSeekPositionInSeconds: Long = 0
    private var lastKnownPlaybackSpeed = 1.0f

    private var entryId: String? = null
    private var sessionId: String? = null
    private var partnerId: String? = null
    private var userAgent: String? = null
    private var deliveryType: String? = null
    private var sessionStartTime: String? = null
    private var referrer: String? = null
    private var currentAudioLanguage: String? = null
    private var currentCaptionLanguage: String? = null
    private var flavorParamsId: String? = null
    private var manifestMaxDownloadTime: Long = -1
    private var segmentMaxDownloadTime: Long = -1
    private var maxConnectDurationMs: Long = -1
    private var totalSegmentDownloadTimeMs: Long = 0
    private var totalSegmentDownloadSizeByte: Long = 0

    private var optionalParams: AMGOptionalParameters? = null
    private var playbackType: AMGMediaEntryType? = null
    private var averageBitrateCounter: AMGAverageBitrateCounter? = null


    private var heatMap = HeatMap()


    private var onApplicationPaused = false
    var audioManager: AudioManager? = null
    private var targetBuffer = 0.0
    private var isLive = false


    init {
        this.context = context
        this.player = player
        userAgent = Utils.getUserAgent(context)
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    /**
     * Apply plugin configuration values.
     *
     * @param pluginConfig - plugin configurations.
     */
    fun onUpdateConfig(pluginConfig: AMGAnalyticsPluginConfig) {
        if (pluginConfig.partnerId != null) {
            partnerId = pluginConfig.partnerId.toString()
        }
        dvrThreshold = pluginConfig.dvrThreshold
        generateReferrer(pluginConfig.referrer)
        optionalParams = AMGOptionalParameters(pluginConfig)
    }

    /**
     * Apply media related values.
     *
     * @param mediaConfig  - media configurations.
     * @param pluginConfig - plugin configurations
     */
    fun onUpdateMedia(mediaConfig: PKMediaConfig, pluginConfig: AMGAnalyticsPluginConfig) {
        averageBitrateCounter = AMGAverageBitrateCounter()
        entryId = populateEntryId(mediaConfig, pluginConfig)
        sessionId = "" //if (player != null && player?.sessionId != null) player?.sessionId else ""
        resetValues()
    }

    private fun populateEntryId(mediaConfig: PKMediaConfig, pluginConfig: AMGAnalyticsPluginConfig): String? {
        var kavaEntryId: String? = null
        if (pluginConfig.entryId != null) {
            kavaEntryId = pluginConfig.entryId
        } else if (isValidMediaEntry(mediaConfig) && mediaConfig.mediaEntry?.metadata != null && mediaConfig.mediaEntry!!.metadata.containsKey("entryId")) {
            kavaEntryId = mediaConfig.mediaEntry!!.metadata["entryId"]
        } else if (isValidMediaEntry(mediaConfig)) {
            kavaEntryId = mediaConfig.mediaEntry?.id
        }
        return kavaEntryId
    }

    private fun isValidMediaEntry(mediaConfig: PKMediaConfig?): Boolean {
        return mediaConfig != null && mediaConfig.mediaEntry != null
    }

    /**
     * Collect all the event relevant information.
     * Information will be hold in [LinkedHashMap] in order to preserve parameters order
     * when sending to server.
     *
     * @param event - current Kava event.
     * @return - Map with all the event relevant information
     */
    fun collectData(event: KavaEvents, mediaEntryType: MediaEntryType, isLiveMedia: Boolean, playheadUpdated: PlayheadUpdated?): Map<String, String?>? {
        isLive = isLiveMedia
        var playerPosition = Consts.POSITION_UNSET.toLong()
        var playerDuration = Consts.TIME_UNSET
        if (!onApplicationPaused) {
            if (playheadUpdated != null) {
                playerPosition = playheadUpdated.position
                playerDuration = playheadUpdated.duration
            }
            playbackType = getPlaybackType(mediaEntryType, playerPosition, playerDuration)
        }
        val params: HashMap<String, String?> = HashMap()
//        params["service"] = "analytics"
//        params["action"] = "trackEvent"
//        params["eventType"] = Integer.toString(event.value)
        params["pid"] = partnerId
        params["eid"] = entryId
        params["sid"] = sessionId
//        params["eventIndex"] = Integer.toString(eventIndex)
        params["rurl"] = referrer
//        params["deliveryType"] = deliveryType
//        params["playbackType"] = playbackType!!.name.toLowerCase(Locale.ROOT)
//        params["clientVer"] = PlayKitManager.CLIENT_TAG
        params["position"] = getPlayerPosition(mediaEntryType, playheadUpdated)
//        params["application"] = context?.packageName
        params["den"] = "$playerDuration"
        params["dhm"] = heatMap.report()
        params["dpl"] = heatMap.uniquePlayTime().toString()
        params["dcn"] = heatMap.totalPlayTime().toString()
//        params["playbackSpeed"] = lastKnownPlaybackSpeed.toString()
//        if (currentCaptionLanguage != null) {
//            params["caption"] = currentCaptionLanguage
//        }
//        if (sessionStartTime != null) {
//            params["sessionStartTime"] = sessionStartTime
//        }
//        when (event) {
//            KavaEvents.VIEW -> {
//                addViewParams(params)
//                addBufferParams(params)
//            }
//            KavaEvents.IMPRESSION -> {
//            }
//            KavaEvents.PLAY -> {
//                params["actualBitrate"] = java.lang.Long.toString(actualBitrate / KB_MULTIPLIER)
//                val joinTime = (System.currentTimeMillis() - joinTimeStartTimestamp) / Consts.MILLISECONDS_MULTIPLIER_FLOAT
//                params["joinTime"] = java.lang.Float.toString(joinTime)
//                val canPlay = (canPlayTimestamp - loadedMetaDataTimestamp) / Consts.MILLISECONDS_MULTIPLIER_FLOAT
//                params["canPlay"] = java.lang.Float.toString(canPlay)
//                params["networkConnectionType"] = Utils.getNetworkClass(context)
//                averageBitrateCounter?.resumeCounting()
//                addBufferParams(params)
//            }
//            KavaEvents.RESUME -> {
//                params["actualBitrate"] = java.lang.Long.toString(actualBitrate / KB_MULTIPLIER)
//                averageBitrateCounter?.resumeCounting()
//                addBufferParams(params)
//            }
//            KavaEvents.SEEK -> params["targetPosition"] = java.lang.Float.toString(targetSeekPositionInSeconds / Consts.MILLISECONDS_MULTIPLIER_FLOAT)
//            KavaEvents.SOURCE_SELECTED, KavaEvents.FLAVOR_SWITCHED -> params["actualBitrate"] = java.lang.Long.toString(actualBitrate / KB_MULTIPLIER)
//            KavaEvents.AUDIO_SELECTED -> params["language"] = currentAudioLanguage
//            KavaEvents.CAPTIONS -> {
//            }
//            KavaEvents.SPEED -> {
//            }
//            KavaEvents.ERROR -> {
//                if (errorCode != -1) {
//                    params["errorCode"] = Integer.toString(errorCode)
//                }
//                if (errorDetails != null) {
//                    params["errorDetails"] = errorDetails
//                }
//                if (errorPosition != null) {
//                    params["errorPosition"] = errorPosition.toString()
//                }
//                errorCode = -1
//                errorDetails = null
//                errorPosition = null
//            }
//            KavaEvents.PAUSE -> {
//                //When player was paused we should update average bitrate value,
//                //because we are interested in average bitrate only during active playback.
//                averageBitrateCounter?.pauseCounting()
//                // each time we stop counting view timer we should reset the sessionStartTimer.
//                sessionStartTime = null
//            }
//        }
        optionalParams?.getParams()?.let{
            params.putAll(it)
        }
        eventIndex++
        return params
    }

    private fun addViewParams(params: MutableMap<String, String?>) {
        if (audioManager != null) {
            val musicVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)
            if (musicVolume == 0 || audioManager?.ringerMode != AudioManager.RINGER_MODE_NORMAL) {
                params["soundMode"] = "1" // sound Off
            } else {
                params["soundMode"] = "2" // sound On
            }
        }
        if (manifestMaxDownloadTime != -1L) {
            params["manifestDownloadTime"] = java.lang.Long.toString(manifestMaxDownloadTime)
            manifestMaxDownloadTime = -1
        }
        if (segmentMaxDownloadTime != -1L) {
            params["segmentDownloadTime"] = java.lang.Long.toString(segmentMaxDownloadTime)
            segmentMaxDownloadTime = -1
        }
        if (totalSegmentDownloadTimeMs > 0 && totalSegmentDownloadSizeByte > 0) {
            val bandwidthInByteMS = totalSegmentDownloadSizeByte / (totalSegmentDownloadTimeMs * 1.0)
            params["bandwidth"] = String.format("%.3f", convertToKbps(bandwidthInByteMS))
            totalSegmentDownloadTimeMs = 0
            totalSegmentDownloadSizeByte = 0
        }
        if (flavorParamsId != null) {
            params["flavorParamsId"] = flavorParamsId // --> in live
        }
        if (targetBuffer == -1.0 && player != null && player?.settings is PlayerSettings) {
            targetBuffer = (player?.settings as PlayerSettings).loadControlBuffers.maxPlayerBufferMs / Consts.MILLISECONDS_MULTIPLIER_FLOAT.toDouble()
        }
        if (targetBuffer > 0) {
            params["targetBuffer"] = targetBuffer.toString() + ""
            if (currentBufferPosition > 0 && currentPosition > 0 && currentBufferPosition > currentPosition) {
                val forwardBufferHealth = (currentBufferPosition - currentPosition) / Consts.MILLISECONDS_MULTIPLIER_FLOAT / targetBuffer
                params["forwardBufferHealth"] = String.format("%.3f", forwardBufferHealth)
            }
        }
        params["networkConnectionType"] = Utils.getNetworkClass(context)
        if (maxConnectDurationMs > 0) {
            params["networkConnectionOverhead"] = (maxConnectDurationMs / Consts.MILLISECONDS_MULTIPLIER_FLOAT).toString() // 	max dns+ssl+tcp resolving time over all video segments in sec
            maxConnectDurationMs = -1
        }
        playTimeSum += AMGStaticAnalyticsObjects.TEN_SECONDS_IN_MS - totalBufferTimePerViewEvent.toLong()
        params["playTimeSum"] = java.lang.Float.toString(playTimeSum / Consts.MILLISECONDS_MULTIPLIER_FLOAT)
        params["actualBitrate"] = java.lang.Long.toString(actualBitrate / KB_MULTIPLIER)
        averageBitrateCounter?.getAverageBitrate(playTimeSum + totalBufferTimePerEntry)?.let{averageBitrate ->
            params["averageBitrate"] = (averageBitrate / KB_MULTIPLIER).toString()
        }

        if (currentAudioLanguage != null) {
            params["audioLanguage"] = currentAudioLanguage
        }
    }

    private fun convertToKbps(bandwidthInByteMS: Double): Double {
        return ((bandwidthInByteMS * 8 // bytes to bits
                / 1024) // bits  to kbits
                * 1000) // msec  to sec
    }


    /**
     * Player track change handler.
     *
     * @param event     - current event.
     * @param trackType - type of the requested track.
     * @return - return true if analytics managed to set newly received track data. Otherwise false.
     */
    fun handleTrackChange(event: PKEvent, trackType: Int): Boolean {
        var shouldSendEvent = true
        when (trackType) {
            Consts.TRACK_TYPE_VIDEO -> {
                if (event is PlaybackInfoUpdated) {
                    val playbackInfo = event.playbackInfo
                    if (actualBitrate == playbackInfo.videoBitrate) {
                        shouldSendEvent = false
                    } else {
                        actualBitrate = playbackInfo.videoBitrate
                    }
                } else {
                    val videoTrackChanged = event as VideoTrackChanged
                    actualBitrate = videoTrackChanged.newTrack.bitrate
                }
                averageBitrateCounter?.setBitrate(actualBitrate)
            }
            Consts.TRACK_TYPE_AUDIO -> {
                val audioTrackChanged = event as AudioTrackChanged
                currentAudioLanguage = audioTrackChanged.newTrack.language
            }
            Consts.TRACK_TYPE_TEXT -> {
                val textTrackChanged = event as TextTrackChanged
                currentCaptionLanguage = textTrackChanged.newTrack.language
            }
        }
        return shouldSendEvent
    }

    /**
     * Player tracks available handler.
     *
     * @param event =  TracksAvailable event.
     */
    fun handleTracksAvailable(event: TracksAvailable) {
        val trackInfo = event.tracksInfo
        if (trackInfo != null) {
            val trackInfoAudioTracks = trackInfo.audioTracks
            val defaultAudioTrackIndex = trackInfo.defaultAudioTrackIndex
            if (defaultAudioTrackIndex < trackInfoAudioTracks.size && trackInfoAudioTracks[defaultAudioTrackIndex] != null) {
                currentAudioLanguage = trackInfoAudioTracks[defaultAudioTrackIndex]?.language
            }
            val trackInfoTextTracks = trackInfo.textTracks
            val defaultTextTrackIndex = trackInfo.defaultTextTrackIndex
            if (defaultTextTrackIndex < trackInfoTextTracks.size && trackInfoTextTracks[defaultTextTrackIndex] != null) {
                currentCaptionLanguage = trackInfoTextTracks[defaultTextTrackIndex]?.language
            }
        }
    }

    fun handleSegmentDownloadTime(event: BytesLoaded) {
        segmentMaxDownloadTime = Math.max(event.loadDuration, segmentMaxDownloadTime)
        totalSegmentDownloadSizeByte += event.bytesLoaded
        totalSegmentDownloadTimeMs += event.loadDuration
    }

    fun handleManifestDownloadTime(event: BytesLoaded) {
        manifestMaxDownloadTime = Math.max(event.loadDuration, manifestMaxDownloadTime)
    }

    fun handleSequenceId(sequenceId: String?) {
        flavorParamsId = sequenceId
    }

    /**
     * Handle error event.
     *
     * @param event - current event.
     */
    fun handleError(event: PKEvent, isFirstPlay: Boolean?, position: Long) {
        var error: PKError? = null
        if (event is Error) {
            error = event.error
        } else if (event is AdEvent.Error) {
            error = event.error
        }
        var errorCode = -1
        if (error?.errorType is PKPlayerErrorType) {
            errorCode = (error.errorType as PKPlayerErrorType).errorCode
            errorDetails = getErrorDetails(event)
            errorPosition = if (isFirstPlay == null) {
                ErrorPositionType.PrePlay.value
            } else {
                if (position > 0) ErrorPositionType.MidStream.value else ErrorPositionType.PrePlaying.value
            }
        } else if (error?.errorType is PKAdErrorType) {
            errorCode = (error.errorType as PKAdErrorType).errorCode
            errorDetails = getAdErrorDetails(event)
        }
        log.e("Playback ERROR. errorCode : $errorCode errorPosition-Type = $errorPosition position = $position")
        this.errorCode = errorCode
    }

    private fun getErrorDetails(event: PKEvent): String? {
        val errorEvent = event as Error
        val errorMetadata = if (errorEvent != null && errorEvent.error != null) errorEvent.error.message else PLAYER_ERROR_STR
        if (errorEvent == null || errorEvent.error == null || errorEvent.error.exception == null) {
            return errorMetadata + "-" + event.eventType().name
        }
        val error = errorEvent.error
        val errorCode = if (error.errorType != null) error.errorType.name + " - " else ""
        val playerErrorException = error.exception as Exception?
        return buildExcptionDetails(errorMetadata, errorCode, playerErrorException)
    }

    private fun getAdErrorDetails(event: PKEvent): String? {
        val errorEvent = event as AdEvent.Error
        val errorMetadata = if (errorEvent != null && errorEvent.error != null) errorEvent.error.message else PLAYER_ERROR_STR
        if (errorEvent == null || errorEvent.error == null || errorEvent.error.exception == null) {
            return errorMetadata + "-" + event.eventType().name
        }
        val error = errorEvent.error
        val errorCode = if (error.errorType != null) error.errorType.name + " - " else ""
        val playerErrorException = error.exception as Exception?
        return buildExcptionDetails(errorMetadata, errorCode, playerErrorException)
    }

    private fun buildExcptionDetails(errorMetadata: String?, errorCode: String, playerErrorException: Exception?): String? {
        var errorMetadata = errorMetadata
        var exceptionClass = ""
        if (playerErrorException?.cause != null) {
            exceptionClass = playerErrorException.cause.toString()
            errorMetadata = playerErrorException.cause.toString()
        } else {
            if (playerErrorException != null && playerErrorException.javaClass != null) {
                exceptionClass = playerErrorException.javaClass.name
            }
        }
        val causeMessages = getExceptionMessageChain(playerErrorException)
        val exceptionCauseBuilder = StringBuilder()
        if (playerErrorException != null && causeMessages.isEmpty()) {
            exceptionCauseBuilder.append(playerErrorException.toString())
        } else {
            for (cause in causeMessages) {
                exceptionCauseBuilder.append(cause).append("\n")
            }
        }
        return "$errorCode$exceptionClass-$exceptionCauseBuilder-$errorMetadata"
    }

    fun getExceptionMessageChain(throwable: Throwable?): LinkedHashSet<String?> {
        var throwable = throwable
        val result = LinkedHashSet<String?>()
        while (throwable != null) {
            if (throwable.message != null) {
                result.add(throwable.message)
            }
            throwable = throwable.cause
        }
        return result
    }

    /**
     * Handle SourceSelected event. Obtain and update current media format
     * accepted by KAVA.
     *
     * @param event - current event.
     */
    fun handleSourceSelected(event: PKEvent) {
        deliveryType = StreamFormat.Url.formatName
        val selectedSource = (event as SourceSelected).source
        if (selectedSource != null && selectedSource.mediaFormat != null) {
            val selectedSourceMediaFormat = selectedSource.mediaFormat
            deliveryType = when (selectedSourceMediaFormat) {
                PKMediaFormat.dash, PKMediaFormat.hls -> selectedSourceMediaFormat.name
                else -> StreamFormat.Url.formatName
            }
        }
    }

    fun handlePlaybackSpeed(event: PlaybackRateChanged) {
        lastKnownPlaybackSpeed = event.rate
    }

    internal enum class ErrorPositionType(val value: Int) {
        PrePlay(3), PrePlaying(1), MidStream(2);

    }

    enum class StreamFormat {
        MpegDash("mpegdash"), AppleHttp("applehttp"), Url("url"), UrlDrm("url+drm"), Unknown;

        var formatName = ""

        constructor() {}
        constructor(name: String) {
            formatName = name
        }

        companion object {
            fun byValue(value: String): StreamFormat {
                for (streamFormat in AMGDataHandler.StreamFormat.values()) {
                    if (streamFormat.formatName == value) {
                        return streamFormat
                    }
                }
                return Unknown
            }
        }
    }

    /**
     * Handle seek event. Update and cache target position.
     *
     * @param event - current event.
     */
    fun handleSeek(event: PKEvent) {
        val seekingEvent = event as Seeking
        targetSeekPositionInSeconds = seekingEvent.targetPosition
        heatMap.movePlayHeadManually(event.currentPosition, event.targetPosition)
    }

    /**
     * Handle player buffering state.
     */
    fun handleBufferingStart() {
        lastKnownBufferingTimestamp = System.currentTimeMillis()
    }

    /**
     * Called when player has finish buffering (PlayerState = READY). When player goes into this state, we should collect all the
     * buffer related information.
     */
    fun handleBufferingEnd() {
        if (lastKnownBufferingTimestamp == 0L) return
        val currentTime = System.currentTimeMillis()
        val bufferTime = currentTime - lastKnownBufferingTimestamp
        totalBufferTimePerViewEvent += bufferTime.toInt()
        totalBufferTimePerEntry += bufferTime
        lastKnownBufferingTimestamp = currentTime
    }

    /**
     * Handles first play.
     */
    fun handleFirstPlay() {
        joinTimeStartTimestamp = System.currentTimeMillis()
    }

    fun handleCanPlay() {
        canPlayTimestamp = System.currentTimeMillis()
    }

    fun handleLoadedMetaData() {
        player?.let{
            heatMap.resetHeatMap(it.duration)
        }
        loadedMetaDataTimestamp = System.currentTimeMillis()
    }

    /**
     * Set view session start time.
     *
     * @param sessionStartTime - sessionStartTime from server.
     */
    fun setSessionStartTime(sessionStartTime: String) {
        if (this.sessionStartTime == null && !sessionStartTime.isEmpty()) {
            this.sessionStartTime = sessionStartTime
        }
    }

    /**
     * When VIEW event was not delivered for more then 30 seconds, Kava server will reset
     * VIEW session. So we also have to do the same.
     */
    fun handleViewEventSessionClosed() {
        eventIndex = 1
        playTimeSum = 0
        sessionStartTime = null
        totalBufferTimePerEntry = 0
        totalBufferTimePerViewEvent = 0
        if (averageBitrateCounter != null) {
            averageBitrateCounter?.reset()
        }
    }

    fun handleConnectionAcquired(event: ConnectionAcquired) {
        if (event.uriConnectionAcquiredInfo != null) {
            maxConnectDurationMs = if (event.uriConnectionAcquiredInfo.connectDurationMs > maxConnectDurationMs) event.uriConnectionAcquiredInfo.connectDurationMs else maxConnectDurationMs
        }
    }

    /**
     * Updates player position based on playbackType. If current playbackType is LIVE or DVR
     * player position will be calculated based on distance from the live edge. Therefore should be 0 or negative value.
     * Otherwise it should be just a real current position.
     *
     * @param mediaEntryType - [KavaMediaEntryType] of the media for the moment of sending event.
     */
    private fun getPlayerPosition(mediaEntryType: MediaEntryType, playheadUpdated: PlayheadUpdated?): String? {
        //When position obtained not from onApplicationPaused state update position/duration.
        if (!onApplicationPaused) {
            if (playheadUpdated == null) {
                currentPosition = 0
                currentBufferPosition = 0
                currentDuration = 0
            } else {
                currentPosition = playheadUpdated.position
                currentBufferPosition = playheadUpdated.bufferPosition
                currentDuration = playheadUpdated.duration
            }
        }
        var playerPosition = currentPosition
        if (mediaEntryType == MediaEntryType.DvrLive || mediaEntryType == MediaEntryType.Live) {
            playerPosition = currentPosition - currentDuration
        }
        return if (playerPosition == 0L) "0" else java.lang.Float.toString(playerPosition / Consts.MILLISECONDS_MULTIPLIER_FLOAT)
    }

    /**
     * Add buffer information to the report.
     *
     * @param params - map of current params.
     */
    private fun addBufferParams(params: MutableMap<String, String?>) {
        val curBufferTimeInSeconds: Float = if (totalBufferTimePerViewEvent == 0) 0.0f else totalBufferTimePerViewEvent / Consts.MILLISECONDS_MULTIPLIER_FLOAT
        val totalBufferTimeInSeconds: Float = if (totalBufferTimePerEntry == 0L) 0.0f else totalBufferTimePerEntry / Consts.MILLISECONDS_MULTIPLIER_FLOAT
        params["bufferTime"] = curBufferTimeInSeconds.toString()
        params["bufferTimeSum"] = totalBufferTimeInSeconds.toString()

        //View event is sent, so reset totalBufferTimePerViewEvent to 0.
        totalBufferTimePerViewEvent = 0
    }

    /**
     * Make a decision what playbackType is active now. First we will try to decide it base on
     * information provided in [KavaAnalyticsConfig]. If for some reason there is no relevant information found,
     * we will rely on player to provide this data.
     * In case when current event of type ERROR - we will concern it as KavaMediaEntryType.Unknown.
     *
     * @return - [KavaMediaEntryType] of the media for the moment of sending event.
     */
    fun getPlaybackType(mediaEntryType: MediaEntryType, playerPosition: Long, playerDuration: Long): AMGMediaEntryType? {
        //If player is null it is impossible to obtain the playbackType, so it will be unknown.
        if (player == null) {
            return AMGMediaEntryType.Unknown
        }
        if (MediaEntryType.DvrLive == mediaEntryType) {
            val distanceFromLive = playerDuration - playerPosition
            return if (distanceFromLive >= dvrThreshold) AMGMediaEntryType.Dvr else AMGMediaEntryType.Live
        } else if (isLive || MediaEntryType.Live == mediaEntryType) {
            return AMGMediaEntryType.Live
        }
        return AMGMediaEntryType.Vod
    }

    /**
     * If provided referrer is null, it will
     * build default one.
     *
     * @param referrer - Custom referrer to set, or null if should use default one.
     */
    private fun generateReferrer(referrer: String?) {
        //If not exist generate default one.
        var referrer = referrer
        if (referrer == null) {
            referrer = buildDefaultReferrer()
        }
        this.referrer = referrer
    }

    /**
     * Build default referrer, will look something like that "app://com.kalura.playkitapplication
     *
     * @return - default referrer.
     */
    private fun buildDefaultReferrer(): String? {
        return "app://" + context?.packageName
    }

    /**
     * Reset all the values for the default.
     * Should happen only once per media entry.
     */
    private fun resetValues() {
        errorCode = -1
        actualBitrate = -1
        sessionStartTime = null
        onApplicationPaused = false
        isLive = false
        lastKnownBufferingTimestamp = 0
        canPlayTimestamp = 0
        loadedMetaDataTimestamp = 0
        manifestMaxDownloadTime = -1
        segmentMaxDownloadTime = -1
        maxConnectDurationMs = -1
        totalSegmentDownloadTimeMs = 0
        totalSegmentDownloadSizeByte = 0
        lastKnownPlaybackSpeed = 1.0f
        targetBuffer = -1.0
        handleViewEventSessionClosed()
    }

    /**
     * @return - user agent value build from application id + playkit version + systems userAgent
     */
    fun getUserAgent(): String? {
        return userAgent
    }

    fun onApplicationPaused(mediaEntryType: MediaEntryType) {
        //Player is destroyed during onApplicationPaused call.
        //So we should update this values before PAUSE event sent.
        player?.let{
            currentDuration = it.duration
            currentBufferPosition = it.bufferedPosition
            currentPosition = it.currentPosition
        }
        playbackType = getPlaybackType(mediaEntryType, currentPosition, currentDuration)
        onApplicationPaused = true
    }

    fun setOnApplicationResumed() {
        onApplicationPaused = false
    }

    fun updateHeatMap(event: PlayheadUpdated) {
            heatMap.updateHeatMap(event.position)
    }

    fun updateSessionID(id: String) {
sessionId = id
    }

    fun handlePlayerReachedEnd() {
        heatMap.markCompleted()
    }
}