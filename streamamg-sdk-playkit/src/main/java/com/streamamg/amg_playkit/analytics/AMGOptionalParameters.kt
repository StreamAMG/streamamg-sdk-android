package com.streamamg.amg_playkit.analytics

import java.util.*

class AMGOptionalParameters(config: AMGAnalyticsConfig) {
    private var optionalParams: HashMap<String, String>? = null

    init {
        optionalParams = HashMap()
        config.playbackContext?.let {
            optionalParams!!["playbackContext"] = it
        }
        config.customVar1?.let {
            optionalParams!!["customVar1"] = it
        }
        config.customVar2?.let {
            optionalParams!!["customVar2"] = it
        }
        config.customVar3?.let {
            optionalParams!!["customVar3"] = it
        }
        config.ks?.let {
            optionalParams!!["ks"] = it
        }
        config.uiconfId?.let {
            if (it != 0) {
                optionalParams!!["uiConfId"] = it.toString()
            }
        }
            config.applicationVersion?.let {
                optionalParams!!["applicationVer"] = it
            }
            config.playlistId?.let {
                optionalParams!!["playlistId"] = it
            }
            config.userId?.let {
                optionalParams!!["userId"] = it
            }
        }

    fun getParams(): HashMap<String, String>? {
        return optionalParams
    }
}