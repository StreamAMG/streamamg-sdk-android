package com.streamamg.streamamg_sdk_cloudmatrix.models


class CloudMatrixSetupModel(
    val userID: String,
    val key: String,
    val url: String,
    val debugURL: String,
    val version: String = "v1",
    val language: String = "en"
)