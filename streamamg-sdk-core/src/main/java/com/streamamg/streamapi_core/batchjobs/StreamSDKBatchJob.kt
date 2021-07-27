package com.streamamg.streamapi_core.batchjobs

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.streamamg.streamapi_core.logging.logErrorCR

/**
 * Core component that services a batch of SDK network requests and fires their callbacks once all the jobs are complete
 * Supports any job that conforms to JobInterface (CloudMatrixJob and StreamPlayJob both conform)
 */
class StreamSDKBatchJob: BatchInterface {
    var jobs: ArrayList<JobInterface> = ArrayList()
    var hasCompleted = false
    var hasFired = false
    var tally = 0

    /**
     * Add a job to the current batch
     */
    fun add(request: JobInterface){
        request.delegate = this
        if (!hasFired){
            jobs.add(request)
        } else {
            logErrorCR("Cannot add a job while the batch is running")
        }
    }

    /**
     * Start the batch jobs running - the jobs will run concurrently
     */
    fun fireBatch(){
        if (!hasFired){
            if (jobs.isEmpty()){
                logErrorCR("There are no jobs to process")
            } else {
                hasFired = true
                hasCompleted = false
                tally = 0
                jobs.forEach {
                    it.reset()
                    it.fireRequest()
                }
                checkCompletion()
            }
        } else {
            logErrorCR("Cannot re-fire while the batch is running")
        }
    }

    private fun checkCompletion() {
        if (hasCompleted){
            return
        }
        var allComplete = true
        jobs.forEach {
            if (!it.isComplete()){
                allComplete = false
            }
        }
        if (allComplete){
            if (!hasCompleted){
                completeJobs()
            }
        } else {
            Handler(Looper.getMainLooper()).postDelayed({ checkCompletion() },500)
        }
    }

    override fun updateTally() {
        tally += 1
        if (tally == jobs.size){
            if (!hasCompleted) {
                completeJobs()
            }
        }
    }

    private fun completeJobs() {
        hasCompleted = true
        hasFired = false
        jobs.forEach {
            it.runCallback()
        }

    }
}