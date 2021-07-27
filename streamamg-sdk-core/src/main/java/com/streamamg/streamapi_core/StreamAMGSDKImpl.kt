package com.streamamg.streamapi_core

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.streamamg.streamapi_core.constants.StreamAPIEnvironment
import com.streamamg.streamapi_core.constants.StreamSDKLogType
import com.streamamg.streamapi_core.logging.StreamSDKLogger
import com.streamamg.streamapi_core.logging.logErrorCR
import com.streamamg.streamapi_core.secure.SecureStorage
import com.streamamg.streamapi_core.secure.StreamSecureStorage
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton object that controls networking, error logging and batch jobs for the SDK modules
 */
class StreamAMGSDKImpl : StreamAMGSDK {

    override lateinit var secureStorage: SecureStorage
    private lateinit var retroFitInstance: Retrofit
    override var environment: StreamAPIEnvironment = StreamAPIEnvironment.PRODUCTION

    /**
     * Method required to initialise the Core component
     *
     * @param context A valid Android context
     * @param okHttpClient (Optional) A custom OKHTTP instance
     * @param gsonImplementation (Optional) A custom Gson instance
     * @param env PRODUCTION or STAGING - defaults to PRODUCTION
     */
    override fun initialise(context: Context, okHttpClient: OkHttpClient?, gsonImplementation: Gson?, env: StreamAPIEnvironment){
        environment = env
        disableLogging()
        StreamPreferences.initialisePreferences(context)
        secureStorage = StreamSecureStorage(context)
        val client: OkHttpClient = okHttpClient ?: OkHttpClient.Builder().build()
        val gson: Gson = gsonImplementation ?: GsonBuilder().create()
        retroFitInstance = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(baseURL())
                .client(client)
                .build()
    }

    private fun baseURL(): String {
        return when (environment) {
            StreamAPIEnvironment.PRODUCTION -> {
                "https://api.streamplay.streamamg.com/"
            }

            else -> {
                "https://staging.api.streamplay.streamamg.com/"
            }
        }
    }

    override fun retroFit(): Retrofit?{
        if (this::retroFitInstance.isInitialized) {
            return retroFitInstance
        }
        return null
    }

    /**
     * Disables all logging or, if requested, individual loggin components
     *
     * @param components A comma separated list of components to be disabled
     */
    override fun disableLogging(vararg components: StreamSDKLogType){
        if (components.isEmpty()){
            StreamSDKLogger.loggingService.turnLoggingOff()
        } else {
            StreamSDKLogger.loggingService.setLoggingForComponents(false, *components)
        }
    }

    /**
     * Enables all logging or, if requested, individual loggin components
     *
     * @param components A comma separated list of components to be enabled
     */
    override fun enableLogging(vararg components: StreamSDKLogType){
        if (environment == StreamAPIEnvironment.PRODUCTION){
            logErrorCR("Logging not enabled for production builds")
            return
        }
        if (components.isEmpty()){
            StreamSDKLogger.loggingService.turnLoggingOn()
        } else {
            StreamSDKLogger.loggingService.setLoggingForComponents(true, *components)
        }
    }
}