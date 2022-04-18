package com.streamamg.amg_playkit.network

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Url
import com.streamamg.amg_playkit.playkitExtensions.MPCategory

interface PlayKitIsLiveAPI {
    @POST()
    fun postIsLive(@Url url: String): Call<String>
    @POST()
    fun postIsNotHarvested(@Url url: String): Call<MPCategory>
}