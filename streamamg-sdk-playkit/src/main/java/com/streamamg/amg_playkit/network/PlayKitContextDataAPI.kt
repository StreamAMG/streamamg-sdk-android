package com.streamamg.amg_playkit.network

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Url
import com.streamamg.amg_playkit.playkitExtensions.MediaContext

interface PlayKitContextDataAPI {
    @POST()
    fun postContextData(@Url url: String): Call<MediaContext>
}
