package com.streamamg.streamapi_streamplay.interfaces

import com.streamamg.streamapi_streamplay.models.StreamPlayIsLiveErrorModel
import com.streamamg.streamapi_streamplay.models.StreamPlayIsLiveModel

interface StreamPlayIsLiveInterface {
    fun isLiveResponseRecieved(model: StreamPlayIsLiveModel)
    fun isLiveErrorRecieved(model: StreamPlayIsLiveErrorModel)
}