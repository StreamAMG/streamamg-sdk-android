package com.streamamg.streamapi_core

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
open class BaseTestClass {

    lateinit var context: Context

    val streamSDK: StreamAMGSDK = StreamAMGSDK.getInstance()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun fakeTestForBaseClass(){
        assertTrue(true)
    }

}