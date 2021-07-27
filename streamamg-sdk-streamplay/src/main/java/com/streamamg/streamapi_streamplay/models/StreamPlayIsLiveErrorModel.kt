package com.streamamg.streamapi_streamplay.models

import com.streamamg.streamapi_streamplay.services.logErrorSP

/**
 * Model returned when an IsLive service is unavailable or reports an error
 */
class StreamPlayIsLiveErrorModel(val liveStreamID: String, val messages: ArrayList<String> = ArrayList(), val errorCode: Int? = null){
    internal fun addMessage(message: String){
        messages.add(message)
    }

    /**
     * Returns an error code - generally HTTP - for a failed call
     * Returns -1 if no code is available
     */
    fun getErrorCode(): Int {
        errorCode?.let{
            return it
        }
        return -1
    }

    /**
     * Returns all error messages in a single String Array
     */
    fun getErrorMessages(): ArrayList<String> {
            return messages
    }

    /**
     * Returns a single string containing all errors reported
     */
    fun getErrorMessagesAsString(): String {
        var errorMessage: String = ""
        for (message :String in messages){
            if (errorMessage.isNotEmpty()){
                errorMessage += " | "
            }
            errorMessage += message
        }
        if (errorMessage.isEmpty()){
            errorMessage = "No messages reported by API"
        }
        return errorMessage
    }

    /**
     * Prints any errors received to the console
     * Logging does not need to be enabled in Core as SDK errors are always delivered
     */
    fun logAllErrors(){
            logErrorSP("Error reported with IsLive ID $liveStreamID")
        errorCode?.let{
            logErrorSP("Error code reported $it")
        }
        messages.forEach {
            logErrorSP(it)
        }
    }

    internal fun logCreationError(){
        logErrorSP("Error creating IsLive URL $liveStreamID")
        messages.forEach {
            logErrorSP(it)
        }
    }
}