package com.streamamg.amg_playkit.models

data class CaptionAssetElement (
    val partnerID: Int?,
    val ks: String?,
    val userID: Int?,
    val objects: List<Object>?,
    val totalCount: Int
)

data class Object (
    val captionParamsID: Int?,
    val language: String?,
    val languageCode: String?,
    val isDefault: Boolean?,
    val label: String?,
    val format: String?,
    val status: Int?,
    val id: String?,
    val entryID: String?,
    val partnerID: Int?,
    val version: String?,
    val size: Int?,
    val tags: String?,
    val fileEXT: String?,
    val createdAt: Int?,
    val updatedAt: Int?,
    val objectDescription: String?
)