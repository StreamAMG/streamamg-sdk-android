package com.streamamg.amg_playkit.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url
import com.streamamg.amg_playkit.playkitExtensions.MPCategory

interface PlayKitIsLiveAPI {
    @GET
    fun getIsLive(@Url url: String): Call<String>
    @GET
    fun getIsNotHarvested(@Url url: String): Call<MPCategory>
}