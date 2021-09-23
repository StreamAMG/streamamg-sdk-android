package com.streamamg.streamapi_core.logging

import android.util.Log
import com.streamamg.streamapi_core.StreamAMGSDK
import com.streamamg.streamapi_core.constants.StreamAPIEnvironment
import com.streamamg.streamapi_core.constants.StreamSDKLogType
import com.streamamg.streamapi_core.constants.StreamSDKLoggingLevel

class StreamSDKLogger() {

    companion object {
        var loggingService: StreamSDKLogger = StreamSDKLogger()
    }

    private val LOG_TAG = "STREAMSDK"
    private var LOGGIN_ON = true
    private var LOG_STANDARD = true
    private var LOG_ERRORS = true
    private var LOG_NETWORK = true
    private var LOG_LONG_LOGS = true
    private var LOG_LISTS = true
    private var LOG_BOOLS = true
    private val LOG_CHUNK_SIZE = 4000
    private val DEFAULT_LOG_LEVEL = StreamSDKLoggingLevel.Debug

    init {
        if (StreamAMGSDK.getInstance().environment == StreamAPIEnvironment.PRODUCTION) {
            LOGGIN_ON = false
            LOG_STANDARD = false
            LOG_ERRORS = false
            LOG_NETWORK = false
            LOG_LONG_LOGS = false
            LOG_LISTS = false
            LOG_BOOLS = false
        }
    }

    fun turnLoggingOff(){
        LOGGIN_ON = false
        LOG_STANDARD = false
        LOG_ERRORS = true
        LOG_NETWORK = false
        LOG_LONG_LOGS = false
        LOG_LISTS = false
        LOG_BOOLS = false
    }

    fun turnLoggingOn(){
        LOGGIN_ON = true
        LOG_STANDARD = true
        LOG_ERRORS = true
        LOG_NETWORK = true
        LOG_LONG_LOGS = true
        LOG_LISTS = true
        LOG_BOOLS = true
    }

    fun setLoggingForComponents(shouldLog: Boolean, vararg components: StreamSDKLogType){
        if (shouldLog){
            LOGGIN_ON = true
        }
        for (type: StreamSDKLogType in components){
            when (type){
              StreamSDKLogType.All -> {
                  if (shouldLog) {
                      turnLoggingOn()
                  } else {
                      turnLoggingOff()
                  }
              }
                StreamSDKLogType.Network ->  LOG_NETWORK = shouldLog
                StreamSDKLogType.BoolValues ->  LOG_BOOLS = shouldLog
                StreamSDKLogType.Lists ->  LOG_LISTS = shouldLog
                StreamSDKLogType.LongLogs ->  LOG_LONG_LOGS = shouldLog
                StreamSDKLogType.Standard ->  LOG_STANDARD = shouldLog
            }
        }
    }

    fun alwaysLog(tag: String, entry: String, level: StreamSDKLoggingLevel = DEFAULT_LOG_LEVEL) {
        when (level) {
            StreamSDKLoggingLevel.Assert -> Log.wtf(tag, entry)
            StreamSDKLoggingLevel.Error -> Log.e(tag, entry)
            StreamSDKLoggingLevel.Warn -> Log.w(tag, entry)
            StreamSDKLoggingLevel.Info -> Log.i(tag, entry)
            StreamSDKLoggingLevel.Debug -> Log.d(tag, entry)
            StreamSDKLoggingLevel.Verbose -> Log.v(tag, entry)
        }
    }

    fun log(entry: String, tag: String = LOG_TAG, level: StreamSDKLoggingLevel = DEFAULT_LOG_LEVEL) {
        if (logsNormalLogEntries()) {
            alwaysLog(tag, entry, level)
        }
    }

    fun logBool(condition: Boolean, description: String, tag: String = LOG_TAG, level: StreamSDKLoggingLevel = DEFAULT_LOG_LEVEL) {
        if (logsBools()) {
            if (condition) {
                alwaysLog(tag, "$description: True", level)
            } else {
                alwaysLog(tag, "$description: False", level)
            }
        }
    }

    fun logNetwork(entry: String, tag: String = LOG_TAG, level: StreamSDKLoggingLevel = DEFAULT_LOG_LEVEL) {
        if (logsNetworkEntries()) {
            alwaysLog("$tag:NW", entry, level)
        }
    }

    fun logLongEntry(entry: String, tag: String = LOG_TAG, level: StreamSDKLoggingLevel = DEFAULT_LOG_LEVEL) {
        if (logsLongEntries()) {
            val lengthOfEntry = entry.length
            var currentStartChunk = 0
            if (lengthOfEntry > LOG_CHUNK_SIZE) {
                var numberOfChunks = lengthOfEntry / LOG_CHUNK_SIZE
                if (lengthOfEntry % LOG_CHUNK_SIZE > 0) {
                    numberOfChunks++
                }
                for (a in 1..numberOfChunks) {
                    val lastPossibleCharacterThisTime = LOG_CHUNK_SIZE * a
                    when {
                        lastPossibleCharacterThisTime < lengthOfEntry -> alwaysLog(
                                tag + ":LG_${a}_of_$numberOfChunks",
                                entry.substring(currentStartChunk, lastPossibleCharacterThisTime),
                                level
                        )
                        else -> alwaysLog(
                                tag + ":LG_${a}_of_$numberOfChunks",
                                entry.substring(currentStartChunk),
                                level
                        )
                    }
                    currentStartChunk += LOG_CHUNK_SIZE
                }
            } else {
                alwaysLog("$tag:LG_1_of_1", entry, level)
            }
        }
    }


    fun logError(entry: String?, tag: String = LOG_TAG, level: StreamSDKLoggingLevel = StreamSDKLoggingLevel.Error) {
        if (logsErrors()) {
            if (entry != null) {
                alwaysLog("$tag:ERROR", entry, level)
            } else {
                alwaysLog("$tag:ERROR", "An unknown error occurred", level)
            }
        }
    }

    fun logList(title: String, tag: String = LOG_TAG, level: StreamSDKLoggingLevel = DEFAULT_LOG_LEVEL) {
        if (logsLists()) {
            alwaysLog("$tag:LIST", title, level)
        }
    }

    fun logList(
            title: String,
            list: HashMap<String, String>,
            tag: String = LOG_TAG,
            level: StreamSDKLoggingLevel = DEFAULT_LOG_LEVEL
    ) {
        if (logsLists()) {
            alwaysLog("$tag:LIST", "----------------", level)
            alwaysLog("$tag:LIST", title, level)
            for (a in list) {
                alwaysLog("$tag:LIST", "${a.key} : ${a.value}", level)
            }
            alwaysLog("$tag:LIST", "----------------", level)
        }
    }

    internal fun logsNormalLogEntries(): Boolean {
        return LOGGIN_ON && LOG_STANDARD
    }

    internal fun logsErrors(): Boolean {
        return LOG_ERRORS
    }

    internal fun logsNetworkEntries(): Boolean {
        return LOGGIN_ON && LOG_NETWORK
    }

    internal fun logsLongEntries(): Boolean {
        return LOGGIN_ON && LOG_LONG_LOGS
    }

    internal fun logsLists(): Boolean {
        return LOGGIN_ON && LOG_LISTS
    }

    internal fun logsBools(): Boolean {
        return LOGGIN_ON && LOG_BOOLS
    }

}