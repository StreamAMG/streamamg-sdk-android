package com.streamamg.streamapi_streamplay.network

import android.util.Log
import com.streamamg.streamapi_core.models.StreamAMGError
import com.streamamg.streamapi_streamplay.models.StreamPlayIsLiveModel
import com.streamamg.streamapi_streamplay.models.StreamPlayRequest
import com.streamamg.streamapi_streamplay.models.StreamPlayResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class StreamPlayCall {

    fun callStreamPlay(request: StreamPlayRequest) {
        val call = streamPlayAPI.getStreamPlay(request.createURL())
        call.enqueue(object : Callback<StreamPlayResponse> {
            override fun onFailure(call: Call<StreamPlayResponse>, t: Throwable) {
                Log.e("StreamPlay", "Call to StreamPlay failed: ${t.localizedMessage}")
            }

            override fun onResponse(call: Call<StreamPlayResponse>, response: Response<StreamPlayResponse>) {
                if (response.isSuccessful) {
                    val model = response.body()
                    model?.let {
                        response(model)
                    }
                } else {
                    response(StreamAMGError(response.code(), response.message()))
                }
            }
        })
        return
    }

    fun callIsLive(url: String) {
        val call = streamPlayIsLiveAPI.getIsLive(url)
        call.enqueue(object : Callback<StreamPlayIsLiveModel> {
            override fun onFailure(call: Call<StreamPlayIsLiveModel>, t: Throwable) {
                Log.e("StreamPlay", "Call to StreamPlay failed: ${t.localizedMessage}")
            }

            override fun onResponse(call: Call<StreamPlayIsLiveModel>, response: Response<StreamPlayIsLiveModel>) {
                if (response.isSuccessful) {
                    val model = response.body()
                    model?.let {
                        response(model)
                    }
                } else {
                    response(StreamAMGError(response.code(), response.message()))
                }
            }
        })
        return
    }

    internal open fun response(response: StreamPlayResponse) {}
    internal open fun response(response: StreamAMGError) {}
    internal open fun response(response: StreamPlayIsLiveModel) {}

    companion object {
        lateinit var streamPlayAPI: StreamPlayAPI
        lateinit var streamPlayIsLiveAPI: StreamPlayIsLiveAPI
    }
}