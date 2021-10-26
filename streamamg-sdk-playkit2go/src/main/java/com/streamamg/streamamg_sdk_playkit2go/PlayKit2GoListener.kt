package com.streamamg.streamamg_sdk_playkit2go

interface PlayKit2GoListener {
    fun downloadDidError(item: PlayKitDownloadItem)
    fun downloadDidUpdate(item: PlayKitDownloadItem)
    fun downloadDidComplete(item: PlayKitDownloadItem)
    fun downloadDidChangeStatus(item: PlayKitDownloadItem)
}