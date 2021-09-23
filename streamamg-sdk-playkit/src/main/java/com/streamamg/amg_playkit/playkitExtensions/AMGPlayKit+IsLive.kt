package com.streamamg.amg_playkit.playkitExtensions

import android.util.Log
import com.streamamg.amg_playkit.AMGPlayKit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun AMGPlayKit.isLive(server: String, entryID: String, callBack: ((Boolean) -> Unit)){
    val url = "$server/api_v3/?service=liveStream&action=islive&id=$entryID&protocol=applehttp&format=1"
        val call = isLiveAPI.postIsLive(url)
        call.enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("AMGISLIVE", "Call to IsLive failed: ${t.localizedMessage}")
                callBack.invoke(false)
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val model = response.body()
                    if (model.equals("true", true)) {
                        callBack.invoke(true)
                    } else {
                        callBack.invoke(false)
                    }
                } else {
                    callBack.invoke(false)
                }
            }
        })
        return
}
