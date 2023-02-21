package com.streamamg.streamamg_sdk_authentication.api

import com.streamamg.streamamg_sdk_authentication.model.UserSummary
import io.reactivex.rxjava3.core.Single
import retrofit2.http.*

interface AuthenticationApi {

    @GET("/api/v1/session/terminate/")
    fun logout(@QueryMap params: Map<String, String>, @Query("apisessionid") sessionId: String): Single<LoginResponse>

    @POST("/api/v1/session/start/")
    fun loginPost(@Body loginRequest: LoginRequest, @QueryMap params: Map<String, String>): Single<LoginResponse>

    @GET("/api/v1/session/ksession/")
    fun getKS(
        @Query("entryId") entryId: String,
        @Query("apisessionid") sessionId: String,
        @QueryMap params: Map<String, String>
    ): Single<LoginResponse>

    @GET("/sso/start")
    fun startSession( @Query("token") token: String, @QueryMap params: Map<String, String>) : Single<LoginResponse>

    @PATCH("/api/v1/account")
    fun updateUserSummary(@Query("apijwttoken") token: String, @Body userSummaryRequest: UserSummary.UserSummaryRequest, @QueryMap params: Map<String, String>) : Single<UserSummary.UserSummaryResponse>

    @GET("/api/v1/account")
    fun getUserSummary(@Query("apijwttoken") token: String, @QueryMap params: Map<String, String>) : Single<UserSummary.UserSummaryResponse>
}