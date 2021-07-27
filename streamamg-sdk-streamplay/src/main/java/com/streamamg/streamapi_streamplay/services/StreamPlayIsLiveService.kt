package com.streamamg.streamapi_streamplay.services

import com.streamamg.streamapi_core.models.StreamAMGError
import com.streamamg.streamapi_streamplay.interfaces.StreamPlayIsLiveInterface
import com.streamamg.streamapi_streamplay.models.StreamPlayIsLiveErrorModel
import com.streamamg.streamapi_streamplay.models.StreamPlayIsLiveModel
import com.streamamg.streamapi_streamplay.models.StreamPlayResponse
import com.streamamg.streamapi_streamplay.network.StreamPlayCall
import java.util.*
import kotlin.collections.ArrayList

/**
 * Singleton object to control an 'IsLive' API call
 */
object StreamPlayIsLiveService{

    private var heartBeat: Timer? = null
    private var calls: ArrayList<IsLiveCall> = ArrayList()
    private var timerRefresh: Long = 30000

    /**
     * Adds an 'IsLive' check to the service with a delegate and, if requested, processes it until it is cancelled
     *
     * @param id A unique identifier for this call
     * @param url The fully formed URL to call
     * @param delegate A class conforming to 'StreamPlayIsLiveInterface' which will handle any response from the IsLive URL
     * @param allowDuplicateURLs defines whether the service should handle the same URL in more than one job - Defaults to 'false'
     * @param shouldRepeat determines whether the IsLive check should be made once, or continually.
     */
    fun addIsLiveCall(id: String, url: String, delegate: StreamPlayIsLiveInterface, allowDuplicateURLs: Boolean = false, shouldRepeat: Boolean = true) {
        if (!shouldRepeat){
            val call = IsLiveCall(id, url, delegate)
            call.checkForPoll()
            return
        }
        checkExistingCalls(id, url, allowDuplicateURLs)?.let{
            it.logCreationError()
            delegate.isLiveErrorRecieved(it)
            return
        }
        val call = IsLiveCall(id, url, delegate)
        call.checkForPoll()
        calls.add(call)
        runTimer()
    }

    /**
     * Adds an 'IsLive' check to the service with a callback and, if requested, processes it until it is cancelled
     *
     * @param id A unique identifier for this call
     * @param url The fully formed URL to call
     * @param callback A function to be run after the job is complete, has the signature '((StreamPlayIsLiveModel?, StreamPlayIsLiveErrorModel?) -> Unit)'
     * @param allowDuplicateURLs defines whether the service should handle the same URL in more than one job - Defaults to 'false'
     * @param shouldRepeat determines whether the IsLive check should be made once, or continually.
     */
    fun addIsLiveCall(id: String, url: String, callBack: (StreamPlayIsLiveModel?, StreamPlayIsLiveErrorModel?) -> Unit, allowDuplicateURLs: Boolean = false, shouldRepeat: Boolean = true) {
        if (!shouldRepeat){
            val call = IsLiveCall(id, url, callBack = callBack)
            call.checkForPoll()
            return
        }
        checkExistingCalls(id, url, allowDuplicateURLs)?.let{
            it.logCreationError()

            callBack.invoke(null, it)
            return
        }
        val call = IsLiveCall(id, url, callBack = callBack)
        call.checkForPoll()
        calls.add(call)
        runTimer()
    }

    /**
     * Adds an 'IsLive' check to the service with a delegate and, if requested, processes it until it is cancelled
     * Returns a unique ID for this call
     *
     * @param url The fully formed URL to call
     * @param delegate A class conforming to 'StreamPlayIsLiveInterface' which will handle any response from the IsLive URL
     * @param allowDuplicateURLs defines whether the service should handle the same URL in more than one job - Defaults to 'false'
     * @param shouldRepeat determines whether the IsLive check should be made once, or continually.
     */
    fun addIsLiveCall(url: String, delegate: StreamPlayIsLiveInterface, allowDuplicateURLs: Boolean = false, shouldRepeat: Boolean = true): String {
        var id = ""
        while (id.isEmpty()){
            id = UUID.randomUUID().toString()
            checkExistingCalls(id, "", true)?.let{
                id = ""
            }
        }
        if (!shouldRepeat){
            val call = IsLiveCall(id, url, delegate)
            call.checkForPoll()
            return id
        }
        if (!allowDuplicateURLs) {
            checkExistingCalls(id, url, false)?.let {
                it.logCreationError()
                delegate.isLiveErrorRecieved(it)
                return id
            }
        }
        val call = IsLiveCall(id, url, delegate)
        call.checkForPoll()
        calls.add(call)
        runTimer()
        return id
    }

    /**
     * Adds an 'IsLive' check to the service with a callback and, if requested, processes it until it is cancelled
     * Returns a unique ID for this call
     *
     * @param url The fully formed URL to call
     * @param callback A function to be run after the job is complete, has the signature '((StreamPlayIsLiveModel?, StreamPlayIsLiveErrorModel?) -> Unit)'
     * @param allowDuplicateURLs defines whether the service should handle the same URL in more than one job - Defaults to 'false'
     * @param shouldRepeat determines whether the IsLive check should be made once, or continually.
     */
    fun addIsLiveCall(url: String, callBack: (StreamPlayIsLiveModel?, StreamPlayIsLiveErrorModel?) -> Unit, allowDuplicateURLs: Boolean = false, shouldRepeat: Boolean = true): String {
        var id = ""
        while (id.isEmpty()){
            id = UUID.randomUUID().toString()
            checkExistingCalls(id, "", true)?.let{
                id = ""
            }
        }
        if (!shouldRepeat){
            val call = IsLiveCall(id, url, callBack = callBack)
            call.checkForPoll()
            return id
        }
        if (!allowDuplicateURLs) {
            checkExistingCalls(id, url, false)?.let {
                it.logCreationError()
                return id
            }
        }
        val call = IsLiveCall(id, url, callBack = callBack)
        call.checkForPoll()
        calls.add(call)
        runTimer()
        return id
    }

    private fun checkExistingCalls(id: String, url: String, allowDuplicateURLs: Boolean, shouldRepeat: Boolean = true): StreamPlayIsLiveErrorModel?{
        var error: StreamPlayIsLiveErrorModel? = null
        calls.forEach { call ->
            if (call.id == id){
                if (error == null){
                    error = StreamPlayIsLiveErrorModel(id)
                }
                error?.let{ isLiveError ->
                    isLiveError.addMessage("Live call ID $id already exists")
                }
            }
            if (!allowDuplicateURLs){
                if (call.url == url){
                    if (error == null){
                        error = StreamPlayIsLiveErrorModel(id)
                    }
                    error?.let{ isLiveError ->
                        isLiveError.addMessage("Live call URL $url already exists - if this is expected, you should call the 'addIsLive' function using 'allowDuplicateURLs=true'")
                    }
                }
            }
        }
        return error
    }

    private fun runTimer(){
        if (calls.isNotEmpty()) {
            if (heartBeat == null) {
                heartBeat = Timer()
            }
            heartBeat?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    pulse()
                }
            }, 1000, timerRefresh)
        }
    }

    /**
     * Stops the IsLive checker running
     */
    fun pauseService(){
        heartBeat?.cancel()
    }

    /**
     * Resumes the IsLive checker
     */
    fun resumeService(){
        runTimer()
    }

    private fun pulse() {
        calls.forEach { call ->
            call.checkForPoll()
        }
    }

    /**
     * Sets the time between 'pulses' - where the service checks for any IsLive calls that need sending
     * Note - this is NOT the time between actual checks, that is handled by the API itself, this is the time between checking each IsLive object to see if it needs to be fired
     *
     * @param pulse The time, in milliseconds, between checks - default is 30000 (30 seconds)
     */
    fun setServicePulse(pulse: Long){
        timerRefresh = pulse
        runTimer()
    }

    /**
     * Removes a particular call from the service
     * The service will be stopped if there are no more calls
     *
     * @param id The id (either submitted to or created by the initial call being added) of the job to be removed
     */
    fun removeCheck(id: String){
        calls.removeAll { x -> x.id == id }
        if (calls.isEmpty()){
            pauseService()
        }
    }

    /**
     * Removes a all calls from the service
     * The service will be stopped
     */
    fun removeAllChecks(){
        calls.clear()
            pauseService()
    }
}