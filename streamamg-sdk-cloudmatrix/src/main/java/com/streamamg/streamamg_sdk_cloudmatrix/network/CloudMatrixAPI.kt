package com.streamamg.streamamg_sdk_cloudmatrix.network

import com.streamamg.streamamg_sdk_cloudmatrix.models.CloudMatrixResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface CloudMatrixAPI {
    @GET
    fun getCloudMatrix(@Url url: String): Call<CloudMatrixResponse>
}