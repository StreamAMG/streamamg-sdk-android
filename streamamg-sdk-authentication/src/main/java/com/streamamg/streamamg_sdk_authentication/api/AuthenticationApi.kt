package com.streamamg.streamamg_sdk_authentication.api

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
}