package com.streamamg.streamapi_core.logging

import com.streamamg.streamapi_core.constants.StreamSDKLoggingLevel

internal fun logCR(data: String, errorLevel: StreamSDKLoggingLevel = StreamSDKLoggingLevel.Debug){
    StreamSDKLogger.loggingService.log(data, "STREAMSDK-CORE", errorLevel)
}

internal fun logNetworkCR(data: String, errorLevel: StreamSDKLoggingLevel = StreamSDKLoggingLevel.Debug){
    StreamSDKLogger.loggingService.logNetwork(data, "STREAMSDK-CORE", errorLevel)
}

internal fun logListCR(data: String, errorLevel: StreamSDKLoggingLevel = StreamSDKLoggingLevel.Debug){
    StreamSDKLogger.loggingService.logList(data, "STREAMSDK-CORE", errorLevel)
}

internal fun logLongCR(data: String, errorLevel: StreamSDKLoggingLevel = StreamSDKLoggingLevel.Debug){
    StreamSDKLogger.loggingService.logLongEntry(data, "STREAMSDK-CORE", errorLevel)
}

internal fun logListCR(data: String, list: HashMap<String, String>, errorLevel: StreamSDKLoggingLevel = StreamSDKLoggingLevel.Debug){
    StreamSDKLogger.loggingService.logList(data, list, "STREAMSDK-CORE", errorLevel)
}

internal fun logBoolCR(data: Boolean, desc: String, errorLevel: StreamSDKLoggingLevel = StreamSDKLoggingLevel.Debug){
    StreamSDKLogger.loggingService.logBool(data, desc, "STREAMSDK-CORE", errorLevel)
}

internal fun logErrorCR(data: String, errorLevel: StreamSDKLoggingLevel = StreamSDKLoggingLevel.Error){
    StreamSDKLogger.loggingService.logError(data, "STREAMSDK-CORE", errorLevel)
}