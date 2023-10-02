package com.streamamg.amg_playkit.playkitExtensions

import android.util.Log
import com.streamamg.amg_playkit.AMGPlayKit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun AMGPlayKit.isLive(server: String, entryID: String, ks: String?, callBack: ((Boolean) -> Unit)) {
    val url = "$server/api_v3/?service=liveStream&action=islive&id=$entryID&protocol=applehttp&format=1"
    val call = isLiveAPI.getIsLive(url)
    call.enqueue(object : Callback<String> {
        override fun onFailure(call: Call<String>, t: Throwable) {
            Log.e("AMGISLIVE", "Call to IsLive failed: ${t.localizedMessage}")
            callBack.invoke(false)
        }

        override fun onResponse(call: Call<String>, response: Response<String>) {
            if (response.isSuccessful) {
                val model = response.body()
                if (model.equals("true", true)) {
                    isNotHarvested(callBack)
                } else {
                    callBack.invoke(false)
                }
            } else {
                callBack.invoke(false)
            }
        }

        private fun isNotHarvested(callBack: (Boolean) -> Unit) {
            if (ks.isNullOrEmpty()) {
                callBack.invoke(true)
                return
            }
            val validURL = "$server/api_v3/?service=baseentry&action=get&entryId=$entryID&protocol=applehttp&format=1&ks=$ks"
            val callHarvested = isLiveAPI.getIsNotHarvested(validURL)
            callHarvested.enqueue(object : Callback<MPCategory> {
                override fun onFailure(call: Call<MPCategory>, t: Throwable) {
                    callBack.invoke(true)
                }

                override fun onResponse(call: Call<MPCategory>, response: Response<MPCategory>) {
                    if (response.isSuccessful) {
                        val resp = response.body()
                        resp?.categories?.let { categories ->
                            if (categories.contains("Harvest")) {
                                callBack.invoke(false)
                                return
                            }
                        }
                        callBack.invoke(true)
                    } else {
                        callBack.invoke(true)
                    }
                }
            })
            return
        }
    })
    return
}

class MPCategory (
    val categories: String?
)
