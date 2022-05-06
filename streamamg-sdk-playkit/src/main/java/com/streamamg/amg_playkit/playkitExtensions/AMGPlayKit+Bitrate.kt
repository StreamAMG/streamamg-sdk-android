package com.streamamg.amg_playkit.playkitExtensions

import android.util.Log
import com.streamamg.amg_playkit.AMGPlayKit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private fun AMGPlayKit.fetchContextData(callBack: ((MediaContext?) -> Unit)) {
    if (currentMediaItem != null && !currentMediaItem?.serverURL.isNullOrEmpty() && !currentMediaItem?.entryID.isNullOrEmpty() && !currentMediaItem?.ks.isNullOrEmpty()) {
        val url = "${currentMediaItem!!.serverURL}/api_v3/?service=baseEntry&action=getContextData&entryId=${currentMediaItem!!.entryID}&${validKS(
            currentMediaItem!!.ks, true)}contextDataParams:objectType=KalturaEntryContextDataParams&contextDataParams:flavorTags=all&format=1"
        val call = contextDataAPI.postContextData(url)
        call.enqueue(object : Callback<MediaContext> {
            override fun onResponse(call: Call<MediaContext>, response: Response<MediaContext>) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        callBack.invoke(response.body())
                        return
                    }
                }
                callBack.invoke(null)
            }

            override fun onFailure(call: Call<MediaContext>, t: Throwable) {
                Log.e("AMGMEDIACONTEXT", "Call to getContextData failed: ${t.localizedMessage}")
                callBack.invoke(null)
            }

        })
    } else {
        callBack.invoke(null)
    }
}

internal fun AMGPlayKit.setBitrate(bitrate: FlavorAsset?){
    if (currentMediaItem != null && bitrate != null) {
        loadMedia(currentMediaItem!!, currentMediaType, currentTime(), bitrate)
    }
}

internal fun AMGPlayKit.updateBitrateSelector() {
    fetchContextData { data ->
        if (data != null) {
            Log.d("BITRATE", "Bitrate received: ${data.fetchBitrates()?.mapNotNull { it.bitrate }.toString() }}")
            controlsView.createBitrateSelector(data.fetchBitrates())
        } else {
            Log.d("BITRATE", "No bitrate received")
            controlsView.createBitrateSelector()
        }
    }
}


class MediaContext (
    val flavorAssets: List<FlavorAsset>?,
) {

//    fun fetchBitrates(): List<Long>? {
//        return flavorAssets?.mapNotNull { it.bitrate }?.sorted()
//    }

    fun fetchBitrates(): List<FlavorAsset>? {
        return flavorAssets?.sortedBy { it.width }
    }
}

class FlavorAsset (
    val width: Long?,
    val height: Long?,
    val bitrate: Long?,
    val id: String?,
    val entryId: String?
)