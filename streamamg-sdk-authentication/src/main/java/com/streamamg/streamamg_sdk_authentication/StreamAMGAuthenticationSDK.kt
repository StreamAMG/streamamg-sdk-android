package com.streamamg.streamamg_sdk_authentication

import android.util.Patterns
import androidx.annotation.VisibleForTesting
import com.google.gson.GsonBuilder
import com.streamamg.streamamg_sdk_authentication.api.AuthenticationApi
import com.streamamg.streamamg_sdk_authentication.api.LoginRequest
import com.streamamg.streamamg_sdk_authentication.api.LoginResponse
import com.streamamg.streamamg_sdk_authentication.extension.applySchedulers
import com.streamamg.streamamg_sdk_authentication.mappers.mapStreamAMGUser
import com.streamamg.streamamg_sdk_authentication.model.Credentials
import com.streamamg.streamamg_sdk_authentication.model.GetKeySessionResult
import com.streamamg.streamamg_sdk_authentication.model.LoginResult
import com.streamamg.streamamg_sdk_authentication.model.LogoutResult
import com.streamamg.streamapi_core.StreamAMGSDK
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

internal class StreamAMGAuthenticationSDK(
    private val coreSDK: StreamAMGSDK,
    @VisibleForTesting var api: AuthenticationApi? = null
) : AuthenticationSDK {

    private var apiUrl: String? = null

    @VisibleForTesting
    var paramsMap = hashMapOf<String, String>()
    private var loginResponse: LoginResponse? = null

    private val disposable = CompositeDisposable()

    private val sessionId
        get() = loginResponse?.currentCustomerSession?.id ?: ""

    override val hasSavedCredentials: Boolean
        get() = getSavedCredentials() != null

    override val lastLoginResponse: LoginResponse?
        get() = loginResponse

    override fun isInitialized(): Boolean {
        return apiUrl?.isNotBlank() ?: false
    }

    override fun initWithURL(url: String, params: String?) {
        initApi(url)
        populateQueryMap(params)
    }

    private fun initApi(url: String) {
        apiUrl = url
        coreSDK.retroFit()?.let {
            api = it
                .newBuilder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .build()
                .create(AuthenticationApi::class.java)
        } ?: throw Exception("Core is not initialised!")
    }

    @VisibleForTesting
    fun populateQueryMap(params: String?) {
        paramsMap.clear()
        val pairs = params?.split("&")
        pairs?.forEach { param ->
            val query = param.split("=")
            if (query.size == 2)
                paramsMap[query.first().replace("?", "")] = query[1]
        }
        Timber.d("Got query Map: $paramsMap")
    }

    override fun isLoggedIn(): Boolean {
        return loginResponse?.currentCustomerSession != null
    }

    override fun logout(callback: (LogoutResult) -> Unit) {

        clearSavedCredentials()

        api?.logout(paramsMap, sessionId)
            ?.applySchedulers()
            ?.subscribeBy(
                onSuccess = {
                    loginResponse = null
                    val user = mapStreamAMGUser(it.currentCustomerSession)
                    user?.let { callback.invoke(LogoutResult.LogoutOK(it)) }
                },
                onError = {
                    callback.invoke(LogoutResult.LogoutFailed(it))
                })
            ?.addTo(disposable)
    }

    override fun loginSyncToken(email: String, password: String): String {
        return Single.create<String> {
            login(email, password) { result ->
                if (result is LoginResult.LoginOK && loginResponse?.authenticationToken != null) {
                    it.onSuccess(loginResponse!!.authenticationToken)
                } else {
                    loginResponse = LoginResponse()
                    loginResponse?.authenticationToken = null
                    it.onError(Exception("Unable to return token"))
                }
            }
        }.blockingGet()
    }

    override fun login(email: String, password: String, callback: (LoginResult) -> Unit) {
        val request = LoginRequest(email, password)

        if (!isValidEmail(email)) {
            callback.invoke(LoginResult.WrongEmailOrPassword("Invalid email"))
            return
        }

        if (!isValidPassword(password)) {
            callback.invoke(LoginResult.WrongEmailOrPassword("Invalid password"))
            return
        }
        api?.loginPost(request, paramsMap)
            ?.applySchedulers()
            ?.subscribeBy(
                onSuccess = { response ->
                    Timber.d("Successfully got LoginResponse: $response")
                    loginResponse = response
                    loginResponse?.let { handleLoginResponse(it, email, password, callback) }
                },
                onError = {
                    callback.invoke(LoginResult.Error(it))
                })
            ?.addTo(disposable)
    }


    override fun loginSilent(callback: (LoginResult) -> Unit) {
        val savedCredentials = getSavedCredentials()
        if (savedCredentials != null) {
            login(savedCredentials.email, savedCredentials.password, callback)
        } else callback.invoke(LoginResult.NoSavedCredentials)
    }

    @VisibleForTesting
    fun handleLoginResponse(response: LoginResponse, email: String, password: String, callback: (LoginResult) -> Unit) {
        if (response.hasErrors) {
            val errors = response.modelErrors
            when {
                errors?.emailaddress != null -> callback.invoke(LoginResult.UserRestriction(errors.emailaddress))
                errors?.password != null -> callback.invoke(LoginResult.UserRestriction(errors.password))
                errors?.restriction != null -> callback.invoke(LoginResult.UserRestriction(errors.restriction))
                errors?.concurrency != null -> callback.invoke(LoginResult.Concurrency(errors.concurrency))
            }
        } else {
            response.authenticationToken?.let { token ->
                saveCredentials(email, password)
                getKS("") { result ->
                    val user = mapStreamAMGUser(response.currentCustomerSession)
                    user?.let { callback.invoke(LoginResult.LoginOK(it)) }
                }
            }
        }
    }

    override fun getKS(entryID: String, callback: (GetKeySessionResult) -> Unit) {
        if (!isInitialized()) {
            callback.invoke(GetKeySessionResult.Error(Exception("PaymentSDK not initialized")))
            return
        }

        api?.getKS(entryID, sessionId, paramsMap)
            ?.applySchedulers()
            ?.subscribeBy(
                onSuccess = { response ->
                    loginResponse = response
                    loginResponse?.let { handleKSLoginResponse(it, callback) } ?: run {
                        callback.invoke(GetKeySessionResult.Error(Exception("Login Response was empty")))
                    }
                },
                onError = {
                    callback.invoke(GetKeySessionResult.Error(it))
                })
            ?.addTo(disposable)

    }

    private fun handleKSLoginResponse(loginResponse: LoginResponse, callback: (GetKeySessionResult) -> Unit) {
        when (loginResponse.status) {
            -1 -> callback.invoke(GetKeySessionResult.Granted(loginResponse.kSession))
            0 -> callback.invoke(GetKeySessionResult.NoActiveSession(loginResponse.errorMessage))
            1 -> callback.invoke(GetKeySessionResult.NoSubscription(loginResponse.errorMessage))
            2 -> callback.invoke(GetKeySessionResult.NoEntitlement(loginResponse.errorMessage))
            3 -> callback.invoke(GetKeySessionResult.Blocked(loginResponse.errorMessage))
            4 -> callback.invoke(GetKeySessionResult.TooManyRequests(loginResponse.errorMessage))
            5 -> callback.invoke(GetKeySessionResult.InvalidEntryId(loginResponse.errorMessage))
            else -> callback.invoke(GetKeySessionResult.Error(Exception(loginResponse.errorMessage)))
        }
    }

    @VisibleForTesting
    fun getSavedCredentials(): Credentials? {
        val email = coreSDK.secureStorage.load(SECURE_KEY_EMAIL)
        val password = coreSDK.secureStorage.load(SECURE_KEY_PASSWORD)
        return if (!email.isNullOrBlank() && !password.isNullOrBlank()) {
            Credentials(email, password)
        } else null
    }

    @VisibleForTesting
    fun clearSavedCredentials() {
        coreSDK.secureStorage.clear(SECURE_KEY_EMAIL)
        coreSDK.secureStorage.clear(SECURE_KEY_PASSWORD)
    }

    @VisibleForTesting
    fun saveCredentials(email: String, password: String) {
        coreSDK.secureStorage.save(SECURE_KEY_EMAIL, email)
        coreSDK.secureStorage.save(SECURE_KEY_PASSWORD, password)
    }

    @VisibleForTesting
    fun isValidPassword(password: String?): Boolean {
        return !password.isNullOrEmpty()
    }

    @VisibleForTesting
    fun isValidEmail(email: String?): Boolean {
        return !email.isNullOrBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    companion object {
        val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").create()
        const val SECURE_KEY_EMAIL = "email"
        const val SECURE_KEY_PASSWORD = "password"
    }

}