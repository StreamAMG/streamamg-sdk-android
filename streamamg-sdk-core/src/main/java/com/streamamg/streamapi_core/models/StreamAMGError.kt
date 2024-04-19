package com.streamamg.streamapi_core.models

/**
 * Model returned when a SDK call is unavailable or reports an error
 */
class StreamAMGError(val code: Int = -1, message: String, throwable: Throwable? = null)
{
        /**
         * Returns an error code - generally HTTP - for a failed call
         * Returns -1 if no code is available
         */
        var messages: ArrayList<String> = ArrayList()
        var internalCode: Int = -1

        fun addMessage(message: String){
                messages.add(message)
        }

        /**
         * Returns a single string containing all errors reported
         */
        fun getMessages(): String {
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

        init {
                internalCode = code
                addMessage(message)
        }

        /**
         * Returns an array containing all errors reported
         */
        fun getAllMessages(): ArrayList<String> {
                return messages
        }

        /**
         * Returns a single string containing all errors reported
         */
        fun getErrorCode(): Int {
                return internalCode
        }

        fun setCode(i: Int) {
                internalCode = i
        }

}