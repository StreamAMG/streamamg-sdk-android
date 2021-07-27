package com.streamamg.streamapi_streamplay.services

import com.streamamg.streamapi_core.batchjobs.BatchInterface
import com.streamamg.streamapi_core.batchjobs.JobInterface
import com.streamamg.streamapi_core.models.StreamAMGError
import com.streamamg.streamapi_streamplay.models.StreamPlayRequest
import com.streamamg.streamapi_streamplay.models.StreamPlayResponse
import com.streamamg.streamapi_streamplay.network.StreamPlayCall

/**
 * A batchable StreamPlay 'job' which can be added to the Core 'StreamSDKBatchJob' component
 *
 * @param request A valid StreamPlayRequest model
 * @param callback (Optional) a function to be run after the job is complete, has the signature '((StreamPlayResponse?, StreamAMGError?) -> Unit)'
 */
class StreamPlayJob(val request: StreamPlayRequest, val callback: ((StreamPlayResponse?, StreamAMGError?) -> Unit)?): StreamPlayCall(), JobInterface {

    private var response: StreamPlayResponse? = null
    private var error: StreamAMGError? = null
    private var completed = false
    override var delegate: BatchInterface? = null

    override fun fireRequest(){
        callStreamPlay(request)
    }

    override fun runCallback(){
        callback?.invoke(response, error)
    }

    override fun response(response: StreamPlayResponse){
        this.response = response
        completed = true
        delegate?.updateTally()
    }

    override fun response(error: StreamAMGError){
        this.error = error
        completed = true
        delegate?.updateTally()
    }

    override fun reset() {
        completed = false
    }

    override fun isComplete(): Boolean {
        return completed
    }


}