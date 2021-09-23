package com.streamamg.amg_playkit.network.isLive

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Url

interface PlayKitIsLiveAPI {
    @POST()
    fun postIsLive(@Url url: String): Call<String>
}