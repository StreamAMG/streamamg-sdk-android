package com.streamamg.amg_playkit.analytics

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*



data class AMGAnalyticsRequest(val eid: String, val pid: Int, val dhm: String, val sid: String) {

    var rurl: String = ""
    var uci: Int = 0
    var den: Long = 0 // Duration of Video
    var tsp: String = date()
    var vlt: Long = 1 // Video load time in Milliseconds
    var emt: Int = 1 // Media Type
    var vls: Int = 1 // Video Load Status
    var dcn: Long = 0 // Duration connected in Seconds
    var dpl: Long = 0 // Duration played in Seconds
    var vnt: Int = 0

    fun date(): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ")
        return format.format(Date())
    }

    fun log() {
        Log.d("AMG", "json: ${toJson()}")
    }

    fun toJson(): String {
        return Gson().toJson(AMGRequestBody(this))
    }

    fun toJsonObject(): JsonObject {
        return Gson().fromJson(toJson(), JsonObject::class.java)
    }
}

data class AMGRequestBody(
    @SerializedName("Data")
    val data: AMGAnalyticsRequest
)