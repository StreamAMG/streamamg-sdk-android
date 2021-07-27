package com.streamamg.streamamg_sdk_cloudmatrix.constants

enum class CloudMatrixQueryType(val query: String, val queryDescription: String) {
    ID("id", "Item ID"),
    MEDIATYPE("mediaData.mediaType", "Media Type"),
    ENTRYID("mediaData.entryId", "Entry ID"),
    ENTRYSTATUS("mediaData.entryStatus", "Entry Status"),
    THUMBNAILURL("mediaData.thumbnailUrl", "Thumbnail URL"),
    BODYTEXT("metaData.body", "Body Text"),
    VIDEODURATION("metaData.VideoDuration", "Video length (in seconds)"),
    TITLETEXT("metaData.title", "Title Text"),
    TAGS("metaData.tags", "Tags"),
    CREATEDDATE("publicationData.createdAt", "Media Creation Date"),
    UPDATEDDATE("publicationData.updatedAt", "Media Last Updated Date"),
    RELEASED("publicationData.released", "Media has been Released"),
    RELEASEFROM("publicationData.releaseFrom", "Media Released From Date"),
    RELEASETO("publicationData.releaseTo", "Media Released To Date")
}