package com.streamamg.streamamg_sdk_authentication

import RxImmediateSchedulerRule
import com.nhaarman.mockitokotlin2.*
import com.streamamg.streamamg_sdk_authentication.api.AuthenticationApi
import com.streamamg.streamamg_sdk_authentication.api.LoginResponse
import com.streamamg.streamamg_sdk_authentication.model.Credentials
import com.streamamg.streamamg_sdk_authentication.model.LoginResult
import com.streamamg.streamapi_core.StreamAMGSDK
import io.reactivex.rxjava3.core.Single
import junit.framework.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import retrofit2.Retrofit

class AuthenticationSDKTest {

    private val coreSdk: StreamAMGSDK = mock()
    private val api: AuthenticationApi = mock()

    private val sdk: StreamAMGAuthenticationSDK = StreamAMGAuthenticationSDK(coreSdk, api)

    @get:Rule
    val schedulers = RxImmediateSchedulerRule()

    @Before
    fun setup() {
        whenever(coreSdk.retroFit()).thenReturn(Retrofit.Builder().baseUrl(URL).build())
        whenever(coreSdk.secureStorage).thenReturn(mock())
        whenever(api.loginPost(any(), any())).thenReturn(Single.just(SUCCESS_LOGIN_RESPONSE))
    }

    @Test
    fun `when api url not provided then sdk is not initialised`() {
        val initialised = sdk.isInitialized()
        assertFalse(initialised)
    }

    @Test
    fun `when api url provided then sdk is initialised`() {
        val params = ""
        sdk.initWithURL(URL, params)
        val initialised = sdk.isInitialized()
        assertTrue(initialised)
    }

    @Test
    fun `when init with params then params added to call`() {
        val params = "lang=en_GB"
        sdk.initWithURL(URL, params)
        val spy = spy(sdk)
        val map = hashMapOf("lang" to "en_GB")
        assertEquals(map, spy.paramsMap)
    }

    @Test
    fun `when init with multiple params then params added to call`() {
        val params = "lang=en_GB&key=value"
        sdk.initWithURL(URL, params)
        val spy = spy(sdk)
        val map = hashMapOf("lang" to "en_GB", "key" to "value")
        assertEquals(map, spy.paramsMap)
    }

    @Test
    fun `when init with multiple params then params added to call and keys stripped`() {
        val params = "?lang=en_GB&key=value"
        sdk.initWithURL(URL, params)
        val spy = spy(sdk)
        val map = hashMapOf("lang" to "en_GB", "key" to "value")
        assertEquals(map, spy.paramsMap)
    }

    @Test
    fun `when email null then return invalid`() {
        val email = null
        val valid = sdk.isValidEmail(email)
        assertFalse(valid)
    }

    @Test
    fun `when email empty then return invalid`() {
        val email = ""
        val valid = sdk.isValidEmail(email)
        assertFalse(valid)
    }

    @Test
    fun `when password empty then return invalid`() {
        val password = ""
        val valid = sdk.isValidPassword(password)
        assertFalse(valid)
    }

    @Test
    fun `when password not empty then return valid`() {
        val valid = sdk.isValidPassword(PASSWORD)
        assertTrue(valid)
    }

    @Test
    fun `when call login then first validate email and password`() {
        val spy = spy(sdk)
        spy.initWithURL(URL, PARAMS)
        whenever(spy.api).thenReturn(mock())
        whenever(spy.isValidEmail(any())).thenReturn(true)
        whenever(spy.isValidPassword(any())).thenReturn(true)
        spy.login(EMAIL, PASSWORD) {}
        verify(spy).isValidEmail(any())
        verify(spy).isValidPassword(any())
    }

    @Test
    fun `when no saved credentials then return false`() {
        val spy = spy(sdk)
        spy.initWithURL(URL, PARAMS)
        whenever(spy.getSavedCredentials()).thenReturn(null)
        assertFalse(spy.hasSavedCredentials)
    }

    @Test
    fun `when login success then save credentials`() {
        val spy = spy(sdk)
        whenever(spy.isValidEmail(any())).thenReturn(true)
        whenever(spy.isValidPassword(any())).thenReturn(true)
        spy.login(EMAIL, PASSWORD) {}
        verify(spy).saveCredentials(EMAIL, PASSWORD)
    }


    @Test
    fun `when logout then clear credentials`() {
        val spy = spy(sdk)
        whenever(api.logout(any(), any())).thenReturn(Single.just(SUCCESS_LOGIN_RESPONSE))
        spy.logout {}
        verify(spy).clearSavedCredentials()
    }

    @Test
    fun `when login silent and no saved credentials then error`() {
        val spy = spy(sdk)
        val callback: ((LoginResult) -> Unit) = mock()
        spy.loginSilent(callback)
        verify(callback).invoke(LoginResult.NoSavedCredentials)
    }

    @Test
    fun `when login silent and saved credentials then perform login`() {
        val spy = spy(sdk)
        Mockito.doReturn(CREDENTIALS).`when`(spy).getSavedCredentials()
        whenever(spy.isValidEmail(any())).thenReturn(true)
        whenever(spy.isValidPassword(any())).thenReturn(true)
        val callback: ((LoginResult) -> Unit) = mock()
        spy.loginSilent(callback)
        verify(spy).login(EMAIL, PASSWORD, callback)
    }


    companion object {
        const val URL = "https://test.url.com/"
        const val PARAMS = "lang=en_GB"
        const val EMAIL = "john@smith.com"
        const val PASSWORD = "12345password"
        val CREDENTIALS = Credentials(EMAIL, PASSWORD)
        val SUCCESS_LOGIN_RESPONSE = LoginResponse(
            authenticationToken = "abcd"
        )
    }

}
