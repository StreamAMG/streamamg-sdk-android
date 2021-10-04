package com.streamamg.streamapi_core

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.streamamg.streamapi_core.constants.StreamAPIEnvironment
import com.streamamg.streamapi_core.constants.StreamSDKLogType
import com.streamamg.streamapi_core.logging.StreamSDKLogger
import junit.framework.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

class StreamAMGCoreInstantiationAndLoggingTests: BaseTestClass() {


    val logger = StreamSDKLogger.loggingService
    
    fun instantiateDebugAndLog() {
            streamSDK.initialise(context, env = StreamAPIEnvironment.STAGING)
            streamSDK.enableLogging()
    }

    @Test
    fun testCoreInstantialtionDev() {
        instantiateDebugAndLog()
        assertEquals(streamSDK.environment, StreamAPIEnvironment.STAGING)
    }

    @Test
    fun testCoreInstantialtionProd() {
        streamSDK.initialise(context, env = StreamAPIEnvironment.PRODUCTION)
        assertEquals(streamSDK.environment, StreamAPIEnvironment.PRODUCTION)
        assertFalse(false)
    }

    @Test
    fun testCoreLoggingEnabled() {
        instantiateDebugAndLog()
        assertTrue(logger.logsBools())
        assertTrue(logger.logsLists())
        assertTrue(logger.logsErrors())
        assertTrue(logger.logsNetworkEntries())
        assertTrue(logger.logsNormalLogEntries())
    }

    @Test
    fun testCoreLoggingDisabled() {
        instantiateDebugAndLog()
        streamSDK.disableLogging()
        assertFalse(logger.logsBools())
        assertFalse(logger.logsLists())
        assertTrue(logger.logsErrors()) // Should always log errors
        assertFalse(logger.logsNetworkEntries())
        assertFalse(logger.logsNormalLogEntries())
    }

    @Test
    fun testCoreLoggingDisabledIndividually() {
        instantiateDebugAndLog()
        assertTrue(logger.logsBools())
        assertTrue(logger.logsLists())
        assertTrue(logger.logsErrors())
        assertTrue(logger.logsNetworkEntries())
        assertTrue(logger.logsNormalLogEntries())
        streamSDK.disableLogging(StreamSDKLogType.BoolValues)
        assertFalse(logger.logsBools())
        assertTrue(logger.logsLists())
        assertTrue(logger.logsErrors())
        assertTrue(logger.logsNetworkEntries())
        assertTrue(logger.logsNormalLogEntries())
        streamSDK.disableLogging(StreamSDKLogType.Lists)
        assertFalse(logger.logsBools())
        assertFalse(logger.logsLists())
        assertTrue(logger.logsErrors())
        assertTrue(logger.logsNetworkEntries())
        assertTrue(logger.logsNormalLogEntries())
        streamSDK.disableLogging(StreamSDKLogType.Network)
        assertFalse(logger.logsBools())
        assertFalse(logger.logsLists())
        assertTrue(logger.logsErrors())
        assertFalse(logger.logsNetworkEntries())
        assertTrue(logger.logsNormalLogEntries())
        streamSDK.disableLogging(StreamSDKLogType.Standard)
        assertFalse(logger.logsBools())
        assertFalse(logger.logsLists())
        assertTrue(logger.logsErrors())
        assertFalse(logger.logsNetworkEntries())
        assertFalse(logger.logsNormalLogEntries())
        streamSDK.disableLogging(StreamSDKLogType.All)
        assertFalse(logger.logsBools())
        assertFalse(logger.logsLists())
        assertTrue(logger.logsErrors()) // Should always log errors
        assertFalse(logger.logsNetworkEntries())
        assertFalse(logger.logsNormalLogEntries())
    }

    @Test
    fun testCoreLoggingEnabledIndividually() {
        instantiateDebugAndLog()
        streamSDK.disableLogging()
        assertFalse(logger.logsBools())
        assertFalse(logger.logsLists())
        assertTrue(logger.logsErrors()) // Should always log errors
        assertFalse(logger.logsNetworkEntries())
        assertFalse(logger.logsNormalLogEntries())
        streamSDK.enableLogging(StreamSDKLogType.BoolValues)
        assertTrue(logger.logsBools())
        assertFalse(logger.logsLists())
        assertTrue(logger.logsErrors()) // Should always log errors
        assertFalse(logger.logsNetworkEntries())
        assertFalse(logger.logsNormalLogEntries())
        streamSDK.enableLogging(StreamSDKLogType.Lists)
        assertTrue(logger.logsBools())
        assertTrue(logger.logsLists())
        assertTrue(logger.logsErrors()) // Should always log errors
        assertFalse(logger.logsNetworkEntries())
        assertFalse(logger.logsNormalLogEntries())
        streamSDK.enableLogging(StreamSDKLogType.Network)
        assertTrue(logger.logsBools())
        assertTrue(logger.logsLists())
        assertTrue(logger.logsErrors()) // Should always log errors
        assertTrue(logger.logsNetworkEntries())
        assertFalse(logger.logsNormalLogEntries())
        streamSDK.enableLogging(StreamSDKLogType.Standard)
        assertTrue(logger.logsBools())
        assertTrue(logger.logsLists())
        assertTrue(logger.logsErrors()) // Should always log errors
        assertTrue(logger.logsNetworkEntries())
        assertTrue(logger.logsNormalLogEntries())
        streamSDK.enableLogging(StreamSDKLogType.All)
        assertTrue(logger.logsBools())
        assertTrue(logger.logsLists())
        assertTrue(logger.logsErrors()) // Should always log errors
        assertTrue(logger.logsNetworkEntries())
        assertTrue(logger.logsNormalLogEntries())
    }
}