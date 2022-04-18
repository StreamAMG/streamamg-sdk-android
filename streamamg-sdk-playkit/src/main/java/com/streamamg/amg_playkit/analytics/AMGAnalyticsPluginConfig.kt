package com.streamamg.amg_playkit.analytics

import com.google.gson.JsonObject
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.utils.Consts

class AMGAnalyticsPluginConfig {
    private val log = PKLog.get(AMGAnalyticsPluginConfig::class.java.simpleName)

    val KS = "ks"
    val BASE_URL = "baseUrl"
    val UICONF_ID = "uiconfId"
    val PARTNER_ID = "partnerId"
    val USER_ID = "userId"
    val CUSTOM_VAR_1 = "customVar1"
    val CUSTOM_VAR_2 = "customVar2"
    val CUSTOM_VAR_3 = "customVar3"
    val APPLICATION_VERSION = "applicationVersion"
    val PLAY_LIST_ID = "playlistId"
    val REFERRER = "referrer"
    val DVR_THRESHOLD = "dvrThreshold"
    val PLAYBACK_CONTEXT = "playbackContext"
    val ENTRY_ID = "entryId"
    val DEFAULT_BASE_URL = "https://stats.mp.streamamg.com/SessionUpdate"

    internal var uiconfId: Int? = null
    internal var partnerId: Int? = null

    internal var ks: String? = null
    internal var referrer: String? = null
    internal var playlistId: String? = null
    internal var entryId: String? = null
    internal var playbackContext: String? = null
    internal var applicationVersion: String? = null
    internal var baseUrl: String? = DEFAULT_BASE_URL
    internal var userId: String? = null
    internal var customVar1: String? = null
    internal var customVar2: String? = null
    internal var customVar3: String? = null

    internal var dvrThreshold = Consts.DISTANCE_FROM_LIVE_THRESHOLD


    // Expecting here the OVP partner Id even for OTT account
    fun setPartnerId(partnerId: Int?): AMGAnalyticsPluginConfig {
        this.partnerId = partnerId
        return this
    }

    fun setBaseUrl(baseUrl: String?): AMGAnalyticsPluginConfig {
        this.baseUrl = baseUrl
        return this
    }

    fun setKs(ks: String?): AMGAnalyticsPluginConfig {
        this.ks = ks
        return this
    }

    fun setEntryId(entryId: String?): AMGAnalyticsPluginConfig {
        this.entryId = entryId
        return this
    }

    fun setDvrThreshold(dvrThreshold: Long): AMGAnalyticsPluginConfig {
        this.dvrThreshold = dvrThreshold
        return this
    }

    fun setUiConfId(uiConfId: Int?): AMGAnalyticsPluginConfig {
        uiconfId = uiConfId
        return this
    }

    fun setUserId(userId: String?): AMGAnalyticsPluginConfig {
        this.userId = userId
        return this
    }

    fun setCustomVar1(customVar1: String?): AMGAnalyticsPluginConfig {
        this.customVar1 = customVar1
        return this
    }

    fun setCustomVar2(customVar2: String): AMGAnalyticsPluginConfig {
        this.customVar2 = customVar2
        return this
    }

    fun setCustomVar3(customVar3: String): AMGAnalyticsPluginConfig {
        this.customVar3 = customVar3
        return this
    }

    fun setReferrer(referrer: String?): AMGAnalyticsPluginConfig {
        this.referrer = referrer
        return this
    }

    fun setPlaybackContext(playbackContext: String?): AMGAnalyticsPluginConfig {
        this.playbackContext = playbackContext
        return this
    }

    fun setPlaylistId(playlistId: String?): AMGAnalyticsPluginConfig {
        this.playlistId = playlistId
        return this
    }

    fun setApplicationVersion(applicationVersion: String?): AMGAnalyticsPluginConfig {
        this.applicationVersion = applicationVersion
        return this
    }

    fun getUiConfId(): Int? {
        return uiconfId
    }

    fun getPartnerId(): Int? {
        return partnerId
    }

    fun getKs(): String? {
        return ks
    }

    fun getEntryId(): String? {
        return entryId
    }

    fun getBaseUrl(): String? {
        return if (baseUrl == null) {
            DEFAULT_BASE_URL
        } else baseUrl
    }

    fun getDvrThreshold(): Long {
        return dvrThreshold
    }

    fun getUserId(): String? {
        return userId
    }

    fun getCustomVar1(): String? {
        return customVar1
    }

    fun getCustomVar2(): String? {
        return customVar2
    }

    fun getCustomVar3(): String? {
        return customVar3
    }

    fun getPlaybackContext(): String? {
        return playbackContext
    }

    fun getPlaylistId(): String? {
        return playlistId
    }

    fun getApplicationVersion(): String? {
        return applicationVersion
    }

    fun getReferrer(): String? {
        return if (isValidReferrer(referrer)) {
            referrer
        } else null
    }

    private fun isValidReferrer(referrer: String?): Boolean {
        return referrer != null && (referrer.startsWith("app://") || referrer.startsWith("http://") || referrer.startsWith("https://"))
    }

    fun isPartnerIdValid(): Boolean {
        return partnerId != null && partnerId != 0
    }

    fun toJson(): JsonObject? {
        val jsonObject = JsonObject()
        jsonObject.addProperty(PARTNER_ID, partnerId)
        jsonObject.addProperty(ENTRY_ID, entryId)
        jsonObject.addProperty(BASE_URL, baseUrl)
        jsonObject.addProperty(DVR_THRESHOLD, dvrThreshold)
        jsonObject.addProperty(KS, ks)
        jsonObject.addProperty(PLAYBACK_CONTEXT, playbackContext)
        jsonObject.addProperty(REFERRER, referrer)
        if (uiconfId != null) {
            jsonObject.addProperty(UICONF_ID, uiconfId)
        }
        jsonObject.addProperty(USER_ID, userId)
        jsonObject.addProperty(CUSTOM_VAR_1, customVar1)
        jsonObject.addProperty(CUSTOM_VAR_2, customVar2)
        jsonObject.addProperty(CUSTOM_VAR_3, customVar3)
        jsonObject.addProperty(PLAY_LIST_ID, playlistId)
        jsonObject.addProperty(APPLICATION_VERSION, applicationVersion)
        return jsonObject
    }
}