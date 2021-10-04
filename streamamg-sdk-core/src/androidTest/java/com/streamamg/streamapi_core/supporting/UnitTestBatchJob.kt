package com.streamamg.streamapi_core.supporting

import com.streamamg.streamapi_core.batchjobs.BatchInterface
import com.streamamg.streamapi_core.batchjobs.JobInterface
import com.streamamg.streamapi_core.models.StreamAMGError

class UnitTestBatchJob(request: Boolean, val shouldComplete: Boolean = true, val callback: ((Boolean) -> Unit)?) : JobInterface {

    private var response: Boolean = false
    private var error: StreamAMGError? = null
    private var completed = false
    override var delegate: BatchInterface? = null

    init {
        response = request
    }

    override fun fireRequest(){
       response = ! response
        if (shouldComplete) {
            completed = true
        }
    }

    override fun runCallback(){
        callback?.invoke(response)
    }

    override fun reset() {
        completed = false
    }

    override fun isComplete(): Boolean {
        return completed
    }


}