package com.streamamg.streamamg_sdk_playkit2go

data class PlayKitDownloads(
    var completed: ArrayList<PlayKitDownloadItem> = ArrayList(),
    var new: ArrayList<PlayKitDownloadItem> = ArrayList(),
    var paused: ArrayList<PlayKitDownloadItem> = ArrayList(),
    var downloading: ArrayList<PlayKitDownloadItem> = ArrayList(),
    var failed: ArrayList<PlayKitDownloadItem> = ArrayList(),
    var metadataLoaded: ArrayList<PlayKitDownloadItem> = ArrayList(),
    var removed: ArrayList<PlayKitDownloadItem> = ArrayList()

) {
    fun percentageForItem(entryID: String): Int {
        completed.find { x -> x.entryID == entryID}?.let {
            return 100
        }
        downloading.find { x -> x.entryID == entryID}?.let {
            return it.percentageComplete().toInt()
        }
        paused.find { x -> x.entryID == entryID}?.let {
            return it.percentageComplete().toInt()
        }
        new.find { x -> x.entryID == entryID}?.let {
            return 0
        }
        metadataLoaded.find { x -> x.entryID == entryID}?.let {
            return 0
        }
        return -1
    }
}

data class PlayKitDownloadItem(
    var entryID: String = "",
    var completedFraction: Float = 0.0f,
    var totalSize: Long = 0,
    var currentDownloadedSize: Long = 0,
    var available: Boolean = false,
    var error: PlayKit2GoError? = null
    )
    {
        fun percentageComplete(): Float {
            return completedFraction// * 100
        }
    }

enum class PlayKit2GoError {
    Already_Queued_Or_Completed, Download_Error, Unknown_Error, Download_Does_Not_Exist, Item_Not_Found, Internal_Error
    }