package com.streamamg.streamapi_streamplay.models

import android.util.Log
import com.streamamg.streamapi_streamplay.services.logNetworkSP
import java.util.*

data class StreamPlayIsLiveModel (
        val isLive: Boolean,
        val pollingFrequency: Int
) {
    var liveStreamID: String = ""

    fun nextPoll(): Long{
        return Date().time + (pollingFrequency * 1000)
    }

    fun logIsLiveModel() {
        logNetworkSP("Is Live: $isLive - NextPoll: ${nextPoll()}")
    }
}