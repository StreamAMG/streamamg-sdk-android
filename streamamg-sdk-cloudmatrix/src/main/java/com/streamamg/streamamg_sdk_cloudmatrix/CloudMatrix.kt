package com.streamamg.streamamg_sdk_cloudmatrix

import com.streamamg.streamamg_sdk_cloudmatrix.constants.CloudMatrixFunction
import com.streamamg.streamamg_sdk_cloudmatrix.models.CloudMatrixRequest
import com.streamamg.streamamg_sdk_cloudmatrix.models.CloudMatrixResponse
import com.streamamg.streamamg_sdk_cloudmatrix.models.CloudMatrixSetupModel
import com.streamamg.streamamg_sdk_cloudmatrix.network.CloudMatrixAPI
import com.streamamg.streamamg_sdk_cloudmatrix.network.CloudMatrixCall
import com.streamamg.streamamg_sdk_cloudmatrix.services.logErrorCM
import com.streamamg.streamapi_core.StreamAMGSDK
import com.streamamg.streamapi_core.models.SearchParameter
import com.streamamg.streamapi_core.models.StreamAMGError

/**
 * This class forms the base CloudMatrix object.
 */
class CloudMatrix() : CloudMatrixCall() {

    private var successCallback: ((CloudMatrixResponse?, StreamAMGError?) -> Unit)? = null
    private var currentResponse: CloudMatrixResponse? = null
    private var currentRequest: CloudMatrixRequest? = null
    private var setupModel: CloudMatrixSetupModel? = null

    /**
     * Secondary constructor, used to configure the module for search capabilities
     *
     * @param userID - Authentication ID
     * @param key - Authentication Key
     * @param url - The base URL of the production CloudMatrix API server
     * @param debugURL - The base URL of the debug / staging CloudMatrix API server - defaults to the production server
     * @param version - The API version to target (defaults to v1)
     * @param language - The language the API returns data in (defaults to English)
     */
    constructor(userID: String, key: String, url: String, debugURL: String? = null, version: String = "v1", language: String = "en") : this() {
        setupModel = CloudMatrixSetupModel(userID, key, url, debugURL ?: url, version, language)
    }

    init {
        cloudMatrixAPI = StreamAMGSDK.getInstance().retroFit()
            ?.newBuilder()
            ?.build()
            ?.create(CloudMatrixAPI::class.java)
            ?: throw Exception("Core is not initialised")
    }


    /**
     * Call the Core networking module and pass a request to the CloudMatrix API
     *
     * @param request - The request model to send to CloudMatrix
     * @param callBack - code to be executed on receipt of a valid response or error
     */
    fun callAPI(request: CloudMatrixRequest, callBack: ((CloudMatrixResponse?, StreamAMGError?) -> Unit)?) {
        request.updateWith(setupModel)
        currentRequest = request
        successCallback = callBack
        callCloudMatrix(request)
    }

    /**
     * Convenience method which calls 'callAPI'
     *
     * @param request - The request model to send to CloudMatrix
     * @param callBack - code to be executed on receipt of a valid response or error
     */
    fun searchAPI(request: CloudMatrixRequest, callBack: ((CloudMatrixResponse?, StreamAMGError?) -> Unit)?) {
        callAPI(request, callBack)
    }

    /**
     * Call the API requesting the previous page (if any)
     */
    fun loadPreviousPage() {
        currentResponse?.let { response ->
            currentRequest?.let { request ->
                val previousPage = response.previousPage()
                if (previousPage < request.currentPage) {
                    request.currentPage = previousPage
                    callCloudMatrix(request)
                }

            }
            return
        }
        logErrorCM("No response available")
    }

    /**
     * Call the API requesting the next page (if any)
     */
    fun loadNextPage() {
        currentResponse?.let { response ->
            currentRequest?.let { request ->
                val nextPage = response.nextPage()
                if (nextPage > request.currentPage) {
                    request.currentPage = nextPage
                    callCloudMatrix(request)
                }
            }
            return
        }
        logErrorCM("No response available")
    }

    /**
     * Reset the object in readiness for a new search / feed
     */
    fun reset() {
        currentRequest = null
        currentResponse = null
    }

    /**
     * Create a new search request using the provided parameters.
     *
     * @param params - An ArrayList of 'SearchParameter' models - See Core documentation
     * @param callBack - code to be executed on receipt of a valid response or error
     */
    fun performSearch(
        params: ArrayList<SearchParameter> = ArrayList(),
        callBack: ((CloudMatrixResponse?, StreamAMGError?) -> Unit)?
    ) {
        val search = CloudMatrixRequest(CloudMatrixFunction.SEARCH, params = params)
        currentRequest = search
        callAPI(search, callBack)
    }

    override fun response(response: CloudMatrixResponse) {
        currentResponse = response
        successCallback?.invoke(response, null)
    }

    override fun response(error: StreamAMGError) {
        successCallback?.invoke(null, error)
    }

    companion object {
        private var instance: CloudMatrix? = null
        fun getInstance(
            userId: String,
            key: String,
            baseUrl: String,
            debugURL: String? = null,
            version: String = "v1",
            language: String = "en"
        ): CloudMatrix {
            return if (instance == null) {
                instance = CloudMatrix(userId, key, baseUrl, debugURL, version, language)
                instance!!
            } else instance!!
        }
    }

}