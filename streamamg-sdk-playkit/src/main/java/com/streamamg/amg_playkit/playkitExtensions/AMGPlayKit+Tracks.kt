package com.streamamg.amg_playkit.playkitExtensions

import android.util.Log
import com.streamamg.amg_playkit.AMGPlayKit
import com.streamamg.amg_playkit.models.CaptionAssetElement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun AMGPlayKit.fetchTracksData(server: String, entryID: String, partnerID: Int, ks: String?, callBack: ((CaptionAssetElement?) -> Unit)) {
    val url = "$server/api_v3/?service=multirequest&format=1&1:service=session&1:action=startWidgetSession&1:widgetId=_$partnerID&2:ks=${if (ks.isNullOrEmpty()) "" else ks}&2:service=caption_captionasset&2:action=list&2:filter:entryIdEqual=$entryID"
    val call = tracksAPI.getTracksData(url)
    call.enqueue(object : Callback<List<CaptionAssetElement>> {
        override fun onFailure(call: Call<List<CaptionAssetElement>>, t: Throwable) {
            Log.e(this.javaClass.simpleName, "Call to getTracksData failed: ${t.localizedMessage}")
            callBack.invoke(null)
        }

        override fun onResponse(call: Call<List<CaptionAssetElement>>, response: Response<List<CaptionAssetElement>>) {
            if (response.isSuccessful) {
                if (response.body() != null) {
                    callBack.invoke(response.body()!!.lastOrNull())
                } else {
                    callBack.invoke(null)
                }
            } else {
                callBack.invoke(null)
            }
        }
    })
}