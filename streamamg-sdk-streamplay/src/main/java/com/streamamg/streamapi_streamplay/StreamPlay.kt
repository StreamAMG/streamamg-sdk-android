package com.streamamg.streamapi_streamplay

import com.streamamg.streamapi_core.StreamAMGSDK
import com.streamamg.streamapi_core.models.SearchParameter
import com.streamamg.streamapi_core.models.StreamAMGError
import com.streamamg.streamapi_streamplay.constants.StreamPlaySport
import com.streamamg.streamapi_streamplay.models.StreamPlayRequest
import com.streamamg.streamapi_streamplay.models.StreamPlayResponse
import com.streamamg.streamapi_streamplay.network.StreamPlayAPI
import com.streamamg.streamapi_streamplay.network.StreamPlayCall
import com.streamamg.streamapi_streamplay.network.StreamPlayIsLiveAPI
import com.streamamg.streamapi_streamplay.services.logErrorSP

/**
 * This class forms the base StreamPlay object.
 *
 * @param partnerID - Default ID of partner to use in all calls
 * @param sport - Default list of sports to use in all calls
 * @param url - The base URL of the production CloudMatrix API server
 * @param debugURL - The base URL of the debug / staging CloudMatrix API server - defaults to the production server
 * @param version - The API version to target (defaults to v1)
 * @param language - The language the API returns data in (defaults to English)
 */
class StreamPlay(val partnerID: String? = null, val sport: ArrayList<StreamPlaySport> = ArrayList()) : StreamPlayCall() {

    private var currentRequest: StreamPlayRequest? = null
    private var currentResponse: StreamPlayResponse? = null
    var successCallback: ((StreamPlayResponse?, StreamAMGError?) -> Unit)? = null

    init {
        streamPlayAPI =
            StreamAMGSDK.getInstance().retroFit()
                ?.create(StreamPlayAPI::class.java)
                ?: throw Exception("Core is not initialised")

        streamPlayIsLiveAPI =
            StreamAMGSDK.getInstance().retroFit()
                ?.create(StreamPlayIsLiveAPI::class.java)
                ?: throw Exception("Core is not initialised")
    }

    /**
     * Call the Core networking module and pass a request to the StreamPlay API
     *
     * @param request - The request model to send to StreamPlay
     * @param callBack - code to be executed on receipt of a valid response or error
     */
    fun callAPI(request: StreamPlayRequest, callBack: ((StreamPlayResponse?, StreamAMGError?) -> Unit)?) {
        successCallback = callBack
        currentRequest = request
        callStreamPlay(request)
    }

    override fun response(response: StreamPlayResponse) {
        currentResponse = response
        successCallback?.invoke(response, null)
    }


    override fun response(error: StreamAMGError) {
        successCallback?.invoke(null, error)
    }

    /**
     * Call the Core networking module and pass a request to the StreamPlay API
     *
     * @param params - array list of 'SearchParameter' objects to add to the request
     * @param callBack - code to be executed on receipt of a valid response or error
     */
    fun performSearch(params: ArrayList<SearchParameter> = ArrayList(), callBack: ((StreamPlayResponse?, StreamAMGError?) -> Unit)?) {
        val request = StreamPlayRequest(params = params, partnerID = partnerID, sport = sport)
        currentRequest = request
        successCallback = callBack
        callStreamPlay(request)
    }

    /**
     * Call the API requesting the previous page (if any)
     */
    fun loadPreviousPage() {
        currentResponse?.let { response ->
            currentRequest?.let { request ->
                val previousPage = response.previousPage()
                if (previousPage < request.currentOffset) {
                    request.currentOffset = previousPage
                    callStreamPlay(request)
                }
            }
            return
        }
        logErrorSP("No response available")
    }

    /**
     * Call the API requesting the next page (if any)
     */
    fun loadNextPage() {
        currentResponse?.let { response ->
            currentRequest?.let { request ->
                val nextPage = response.nextPage()
                if (nextPage > request.currentOffset) {
                    request.currentOffset = nextPage
                    callStreamPlay(request)
                }
            }
            return
        }
        logErrorSP("No response available")
    }

}