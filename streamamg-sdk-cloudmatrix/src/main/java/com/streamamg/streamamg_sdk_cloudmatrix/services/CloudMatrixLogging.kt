package com.streamamg.streamamg_sdk_cloudmatrix.services

import com.streamamg.streamapi_core.constants.StreamSDKLoggingLevel
import com.streamamg.streamapi_core.logging.StreamSDKLogger

internal fun logCM(data: String, errorLevel: StreamSDKLoggingLevel = StreamSDKLoggingLevel.Debug){
    StreamSDKLogger.loggingService.log(data, "STREAMSDK-CLOUDMATRIX", errorLevel)
}

internal fun logNetworkCM(data: String, errorLevel: StreamSDKLoggingLevel = StreamSDKLoggingLevel.Debug){
    StreamSDKLogger.loggingService.logNetwork(data, "STREAMSDK-CLOUDMATRIX", errorLevel)
}

internal fun logListCM(data: String, errorLevel: StreamSDKLoggingLevel = StreamSDKLoggingLevel.Debug){
    StreamSDKLogger.loggingService.logList(data, "STREAMSDK-CLOUDMATRIX", errorLevel)
}

internal fun logLongCM(data: String, errorLevel: StreamSDKLoggingLevel = StreamSDKLoggingLevel.Debug){
    StreamSDKLogger.loggingService.logLongEntry(data, "STREAMSDK-CLOUDMATRIX", errorLevel)
}

internal fun logListCM(data: String, list: HashMap<String, String>, errorLevel: StreamSDKLoggingLevel = StreamSDKLoggingLevel.Debug){
    StreamSDKLogger.loggingService.logList(data, list, "STREAMSDK-CLOUDMATRIX", errorLevel)
}

internal fun logBoolCM(data: Boolean, desc: String, errorLevel: StreamSDKLoggingLevel = StreamSDKLoggingLevel.Debug){
    StreamSDKLogger.loggingService.logBool(data, desc, "STREAMSDK-CLOUDMATRIX", errorLevel)
}

internal fun logErrorCM(data: String, errorLevel: StreamSDKLoggingLevel = StreamSDKLoggingLevel.Error){
    StreamSDKLogger.loggingService.logError(data, "STREAMSDK-CLOUDMATRIX", errorLevel)
}