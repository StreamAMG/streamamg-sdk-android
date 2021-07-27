package com.streamamg.streamapi_streamplay.network

import com.streamamg.streamapi_streamplay.models.StreamPlayResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface StreamPlayAPI {
    @GET()
    fun getStreamPlay(@Url url: String): Call<StreamPlayResponse>
}