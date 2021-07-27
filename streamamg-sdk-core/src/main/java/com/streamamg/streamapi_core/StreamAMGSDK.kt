package com.streamamg.streamapi_core

import android.content.Context
import com.google.gson.Gson
import com.streamamg.streamapi_core.constants.StreamAPIEnvironment
import com.streamamg.streamapi_core.constants.StreamSDKLogType
import com.streamamg.streamapi_core.secure.SecureStorage
import okhttp3.OkHttpClient
import retrofit2.Retrofit

interface StreamAMGSDK {

    var environment: StreamAPIEnvironment
    var secureStorage: SecureStorage

    fun initialise(
        context: Context,
        okHttpClient: OkHttpClient? = null,
        gsonImplementation: Gson? = null,
        env: StreamAPIEnvironment = StreamAPIEnvironment.PRODUCTION
    )

    fun retroFit(): Retrofit?
    fun disableLogging(vararg components: StreamSDKLogType = arrayOf(StreamSDKLogType.All))
    fun enableLogging(vararg components: StreamSDKLogType = arrayOf(StreamSDKLogType.All))

    companion object {
        private val sdk: StreamAMGSDK by lazy { StreamAMGSDKImpl() }
        fun getInstance(): StreamAMGSDK {
            return sdk
        }
    }
}