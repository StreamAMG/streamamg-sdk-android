package com.streamamg.streamapi_streamplay.services

import com.streamamg.streamapi_core.constants.StreamSDKLoggingLevel
import com.streamamg.streamapi_core.logging.StreamSDKLogger

internal fun logSP(data: String, errorLevel: StreamSDKLoggingLevel = StreamSDKLoggingLevel.Debug){
    StreamSDKLogger.loggingService.log(data, "STREAMSDK-STREAMPLAY", errorLevel)
}

internal fun logNetworkSP(data: String, errorLevel: StreamSDKLoggingLevel = StreamSDKLoggingLevel.Debug){
    StreamSDKLogger.loggingService.logNetwork(data, "STREAMSDK-STREAMPLAY", errorLevel)
}

internal fun logListSP(data: String, errorLevel: StreamSDKLoggingLevel = StreamSDKLoggingLevel.Debug){
    StreamSDKLogger.loggingService.logList(data, "STREAMSDK-STREAMPLAY", errorLevel)
}

internal fun logLongSP(data: String, errorLevel: StreamSDKLoggingLevel = StreamSDKLoggingLevel.Debug){
    StreamSDKLogger.loggingService.logLongEntry(data, "STREAMSDK-STREAMPLAY", errorLevel)
}

internal fun logListSP(data: String, list: HashMap<String, String>, errorLevel: StreamSDKLoggingLevel = StreamSDKLoggingLevel.Debug){
    StreamSDKLogger.loggingService.logList(data, list, "STREAMSDK-STREAMPLAY", errorLevel)
}

internal fun logBoolSP(data: Boolean, desc: String, errorLevel: StreamSDKLoggingLevel = StreamSDKLoggingLevel.Debug){
    StreamSDKLogger.loggingService.logBool(data, desc, "STREAMSDK-STREAMPLAY", errorLevel)
}

internal fun logErrorSP(data: String, errorLevel: StreamSDKLoggingLevel = StreamSDKLoggingLevel.Error){
    StreamSDKLogger.loggingService.logError(data, "STREAMSDK-STREAMPLAY", errorLevel)
}