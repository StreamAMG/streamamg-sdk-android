package com.streamamg.streamapi_core

import com.streamamg.streamapi_core.batchjobs.BatchInterface
import com.streamamg.streamapi_core.batchjobs.StreamSDKBatchJob
import com.streamamg.streamapi_core.supporting.UnitTestBatchJob
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test

class StreamAMGCoreBatchJobTests: BaseTestClass() {

    var batchJob: StreamSDKBatchJob = StreamSDKBatchJob()
    
    @Test
    fun testBatchJobs() {
       // batchJob.delegate = this
        var trueCompletion = UnitTestBatchJob(false) {result ->
                assertTrue(result)
        }
        var falseCompletion = UnitTestBatchJob(true) {result ->
                assertFalse(result)
        }
        batchJob.add(trueCompletion)
        batchJob.add(falseCompletion)
        assertTrue(batchJob.jobs.size == 2)
        batchJob.fireBatch()
        batchJob.jobs.clear() // Should be removeJobs()
        assertTrue(batchJob.jobs.isEmpty())
        trueCompletion = UnitTestBatchJob(false) {result ->
                assertTrue(result)
        }
        falseCompletion = UnitTestBatchJob(true) {result ->
                assertFalse(result)
        }
        batchJob.add(trueCompletion)
        batchJob.add(falseCompletion)
//        batchJob.fireBatch(true)
//        assertTrue(batchJob.jobs.isEmpty())
    }

    @Test
    fun testBatchJobsCannotAddWhileProcessing() {
       // batchJob.delegate = this // TODO: Implement BatchJobCompletionInterface
        var trueCompletion = UnitTestBatchJob(false, false) {result ->
                assertTrue(result)
        }
        var falseCompletion = UnitTestBatchJob(true) {result ->
                assertFalse(result)
        }
        var falseCompletion2 = UnitTestBatchJob(true) {result ->
                assertFalse(result)
        }
        batchJob.add(trueCompletion)
        batchJob.add(falseCompletion)
        assertTrue(batchJob.jobs.size == 2)
        batchJob.fireBatch()
        batchJob.add(falseCompletion2)
        assertTrue(batchJob.jobs.size == 2)
        batchJob.jobs.clear() // Should be removeJobs()
        trueCompletion = UnitTestBatchJob(false) {result ->
                assertTrue(result)
        }
        falseCompletion = UnitTestBatchJob(true) {result ->
                assertFalse(result)
        }
        falseCompletion2 = UnitTestBatchJob(true) {result ->
                assertFalse(result)
        }
        batchJob.add(trueCompletion)
        batchJob.add(falseCompletion)
        batchJob.add(falseCompletion2)
        batchJob.fireBatch()
 //       assertFalse(batchJob.jobs.isEmpty())
    }

    @Test
    fun testBatchJobsCannotRefireOrFireWhenEmpty(){
        var trueCompletion = UnitTestBatchJob(false, false,  null)
        var falseCompletion = UnitTestBatchJob(true, callback = null)
        batchJob.fireBatch()
        batchJob.add(trueCompletion)
        batchJob.add(falseCompletion)
        batchJob.fireBatch()
        batchJob.fireBatch()
        batchJob.jobs.clear() // Should be removeJobs()
        trueCompletion = UnitTestBatchJob(false) {result ->
                assertTrue(result)
        }
        falseCompletion = UnitTestBatchJob(true) {result ->
                assertFalse(result)
        }
        batchJob.add(trueCompletion)
        batchJob.add(falseCompletion)
//        batchJob.fireBatch(true)   //TODO: Implement firebatch: Clear jobs
//        assertTrue(batchJob.jobs.isEmpty())
    }

    fun batchJobsCompleted() {
        assertTrue(true)
    }
}