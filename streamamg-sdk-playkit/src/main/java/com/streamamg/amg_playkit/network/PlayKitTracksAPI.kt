package com.streamamg.amg_playkit.network

import com.streamamg.amg_playkit.models.CaptionAssetElement
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface PlayKitTracksAPI {
    @GET
    fun getTracksData(@Url url: String): Call<List<CaptionAssetElement>>
}