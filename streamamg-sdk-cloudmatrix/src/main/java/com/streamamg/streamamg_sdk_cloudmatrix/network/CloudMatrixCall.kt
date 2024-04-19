package com.streamamg.streamamg_sdk_cloudmatrix.network

import com.streamamg.streamamg_sdk_cloudmatrix.models.CloudMatrixRequest
import com.streamamg.streamamg_sdk_cloudmatrix.models.CloudMatrixResponse
import com.streamamg.streamamg_sdk_cloudmatrix.services.logErrorCM
import com.streamamg.streamapi_core.models.StreamAMGError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class CloudMatrixCall {
    internal fun callCloudMatrix(request: CloudMatrixRequest) {
        val call = cloudMatrixAPI.getCloudMatrix(request.createURL())
        call.enqueue(object : Callback<CloudMatrixResponse> {
            override fun onFailure(call: Call<CloudMatrixResponse>, t: Throwable) {
                logErrorCM("Call to CloudMatrix failed: ${t.localizedMessage}")

                response(StreamAMGError(message = t.message.orEmpty(), throwable = t))
            }

            override fun onResponse(call: Call<CloudMatrixResponse>, response: Response<CloudMatrixResponse>) {
                if (response.isSuccessful) {
                    val model = response.body()
                    if(model != null) {
                        response(model)
                    } else {
                        response(StreamAMGError(response.code(), response.message().orEmpty()))
                    }
                } else {
                    response(StreamAMGError(response.code(), response.message()))
                }
            }
        })
        return
    }

    internal open fun response(response: CloudMatrixResponse) {}
    internal open fun response(response: StreamAMGError) {}

    companion object {
        lateinit var cloudMatrixAPI: CloudMatrixAPI
    }
}