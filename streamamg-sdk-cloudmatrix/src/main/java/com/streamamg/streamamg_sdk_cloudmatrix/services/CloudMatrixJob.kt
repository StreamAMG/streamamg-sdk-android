package com.streamamg.streamamg_sdk_cloudmatrix.services

import com.streamamg.streamamg_sdk_cloudmatrix.models.CloudMatrixRequest
import com.streamamg.streamamg_sdk_cloudmatrix.models.CloudMatrixResponse
import com.streamamg.streamamg_sdk_cloudmatrix.network.CloudMatrixCall
import com.streamamg.streamapi_core.batchjobs.BatchInterface
import com.streamamg.streamapi_core.batchjobs.JobInterface
import com.streamamg.streamapi_core.models.StreamAMGError

/**
 * A batchable CloudMatrix 'job' which can be added to the Core 'StreamSDKBatchJob' component
 *
 * @param request A valid CloudMatrixRequest model
 * @param callback (Optional) a function to be run after the job is complete, has the signature '((CloudMatrixResponse?, StreamAMGError?) -> Unit)'
 */
class CloudMatrixJob(
    private val request: CloudMatrixRequest,
    private val callback: ((CloudMatrixResponse?, StreamAMGError?) -> Unit)?
) : CloudMatrixCall(), JobInterface {

    private var response: CloudMatrixResponse? = null
    private var error: StreamAMGError? = null
    private var completed = false
    override var delegate: BatchInterface? = null

    override fun fireRequest() {
        callCloudMatrix(request)
    }

    override fun runCallback() {
        callback?.invoke(response, error)
    }

    override fun response(response: CloudMatrixResponse) {
        this.response = response
        completed = true
        delegate?.updateTally()
    }

    override fun response(error: StreamAMGError) {
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