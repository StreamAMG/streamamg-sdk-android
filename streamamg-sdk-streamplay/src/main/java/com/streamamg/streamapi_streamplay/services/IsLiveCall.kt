package com.streamamg.streamapi_streamplay.services

import com.streamamg.streamapi_core.models.StreamAMGError
import com.streamamg.streamapi_streamplay.interfaces.StreamPlayIsLiveInterface
import com.streamamg.streamapi_streamplay.models.StreamPlayIsLiveErrorModel
import com.streamamg.streamapi_streamplay.models.StreamPlayIsLiveModel
import com.streamamg.streamapi_streamplay.network.StreamPlayCall
import java.util.*

internal class IsLiveCall(val id: String, val url: String, val delegate: StreamPlayIsLiveInterface?, var mainCallback: ((StreamPlayIsLiveModel?, StreamPlayIsLiveErrorModel?) -> Unit)? = null): StreamPlayCall()  {

    constructor(id: String, url: String, callBack: (StreamPlayIsLiveModel?, StreamPlayIsLiveErrorModel?) -> Unit) : this(id,url,null, mainCallback = callBack)

    var nextPoll: Long = 0


    fun checkForPoll(){
        if (Date().time > nextPoll){
            nextPoll += 30000
            callIsLive(url)
        }
    }

    override fun response(response: StreamPlayIsLiveModel){
        nextPoll = response.nextPoll()
        response.liveStreamID = id
        delegate?.isLiveResponseRecieved(response)
        mainCallback?.invoke(response, null)
    }


    override fun response(error: StreamAMGError){
        val errorRtn = StreamPlayIsLiveErrorModel(id, error.messages, error.code)
        delegate?.isLiveErrorRecieved(errorRtn)
        mainCallback?.invoke(null, errorRtn)
    }


}