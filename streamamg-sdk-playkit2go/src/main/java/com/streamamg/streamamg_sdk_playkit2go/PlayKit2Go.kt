package com.streamamg.streamamg_sdk_playkit2go

import android.content.Context
import android.util.Log
import com.kaltura.dtg.ContentManager
import com.kaltura.dtg.DownloadItem
import com.kaltura.dtg.DownloadStateListener
import java.lang.Exception
import android.text.TextUtils
import com.kaltura.dtg.DownloadState
import java.lang.StringBuilder
import java.text.NumberFormat


class PlayKit2Go: DownloadStateListener{

    private var context: Context? = null
    private var cm: ContentManager? = null
    private var listener: PlayKit2GoListener? = null


    private var allValidIDs: ArrayList<String> = ArrayList()

    companion object {
        private val sdk: PlayKit2Go by lazy { PlayKit2Go() }
        fun getInstance(): PlayKit2Go {
            return sdk
        }
    }

    fun setup(context: Context) {
        this.context = context
        cm = null
        cm = ContentManager.getInstance(context)
      //  cm?.settings?.maxConcurrentDownloads = 3
        cm?.addDownloadStateListener(this)
        cm?.start {
            cm?.let{
                for (item in it.getDownloads(DownloadState.NEW, DownloadState.INFO_LOADED, DownloadState.IN_PROGRESS, DownloadState.COMPLETED, DownloadState.FAILED, DownloadState.PAUSED)) {
                    //   itemStateChanged(item)
                    Log.d("AMGD2G", "Item: ${item.itemId} is in state: ${item.state}")
                    processState(item)
                }
            }

         //   setListAdapter(itemArrayAdapter)
        }

    }

    private fun processState(item: DownloadItem) {
        when (item.state){
            DownloadState.NEW -> {
                item.loadMetadata()
            }
            DownloadState.INFO_LOADED -> item.startDownload()
            DownloadState.IN_PROGRESS -> {
            }
            DownloadState.PAUSED -> {
            }
            DownloadState.COMPLETED -> {
            }
            DownloadState.FAILED -> {
            }
        }
    }

    fun destroy() {
        cm?.removeDownloadStateListener(this)
        cm?.stop()
    }

    fun download(serverUrl: String, partnerID: Int, entryID: String, ks: String? = null){
        Log.d("AMGD2G", "KS: $ks")
        var item = cm?.findItem(entryID)
        if (item == null) {
            item = cm?.createItem(entryID, getDownloadURL(serverUrl, partnerID, entryID, ks))
        }
        item?.loadMetadata()

    }

    fun remove(entryID: String) {
        try {
            cm?.removeItem(entryID)
        } catch (e: Exception) {
            Log.e("AMGD2G", "Can't remove item - ${e.localizedMessage}")
        }
    }

    fun pause() {
        cm?.pauseDownloads()
    }

    fun resume() {
        cm?.resumeDownloads()
    }

    fun setListener(listener: PlayKit2GoListener){
        this.listener = listener
    }

    private fun getDownloadURL(serverUrl: String, partnerID: Int, entryID: String, ks: String? = null): String {
        return "$serverUrl/p/$partnerID/sp/0/playManifest/entryId/$entryID/format/applehttp/${validKS(ks)}protocol/https/manifest.m3u8"
    }

    private fun validKS(ks: String?): String {
        ks?.let {
            if (it.isEmpty()){
                return ""
            }
            return "ks/$it/"
        }
        return ""
    }

    override fun onDownloadComplete(item: DownloadItem?) {
        item?.let {item ->
            Log.d("AMGD2G", "Download complete for ${item.itemId}")
            listener?.downloadDidComplete(PlayKitDownloadItem(item.itemId, 1.0f, item.downloadedSizeBytes,  item.downloadedSizeBytes, true,null))
        }

    }

    override fun onProgressChange(item: DownloadItem?, downloadedBytes: Long) {
        item?.let { item ->
            listener?.downloadDidUpdate(PlayKitDownloadItem(item.itemId, item.estimatedCompletionPercent, item.estimatedSizeBytes,  item.downloadedSizeBytes, true, null))
        }
    }

    override fun onDownloadStart(item: DownloadItem?) {
        item?.let { item ->
            Log.d("AMGD2G", "Download started for ${item.itemId}")
        }
    }

    override fun onDownloadPause(item: DownloadItem?) {
        item?.let { item ->
            Log.d("AMGD2G", "Download paused for ${item.itemId}")
        }
    }

    override fun onDownloadFailure(item: DownloadItem?, error: Exception?) {
        item?.let { item ->
            Log.d("AMGD2G", "Download failed for ${item.itemId}")
        }

    }

    override fun onDownloadMetadata(item: DownloadItem?, error: Exception?) {
        Log.d("AMGD2G", "Metadate downloaded")
        item?.let {
            Log.d("AMGD2G", "Metadate downloaded for ${item.itemId}")
            item.startDownload()
            var tracks: ArrayList<DownloadItem.Track> = ArrayList()
            val trackSelector = item.trackSelector ?: return

            val boolSelectedTracks: MutableList<Boolean> = ArrayList()
            val trackNames: MutableList<String> = ArrayList()
            val numberFormat: NumberFormat = NumberFormat.getIntegerInstance()

            for (type in DownloadItem.TrackType.values()) {
                val availableTracks = trackSelector.getAvailableTracks(type)
                val selectedTracks = trackSelector.getSelectedTracks(type)
                for (track in availableTracks) {
                    tracks.add(track)
                    boolSelectedTracks.add(selectedTracks.contains(track))
                    val sb = StringBuilder(track.type.name)
                    val bitrate = track.bitrate
                    if (bitrate > 0) {
                        sb.append(" | ").append(numberFormat.format(bitrate))
                    }
                    if (track.type == DownloadItem.TrackType.VIDEO) {
                        sb.append(" | ").append(track.width).append("x").append(track.height)
                    }
                    val language = track.language
                    if (!TextUtils.isEmpty(language)) {
                        sb.append(" | ").append(language)
                    }
                    trackNames.add(sb.toString())
                }
            }
            val allTrackNames = trackNames.toTypedArray()
            val selected = BooleanArray(boolSelectedTracks.size)
            for (i in boolSelectedTracks.indices) {
                selected[i] = boolSelectedTracks[i]
            }


        }
    }

    override fun onTracksAvailable(
        item: DownloadItem?,
        trackSelector: DownloadItem.TrackSelector?
    ) {
        trackSelector?.setSelectedTracks(DownloadItem.TrackType.AUDIO, trackSelector.getAvailableTracks(DownloadItem.TrackType.AUDIO))
        trackSelector?.setSelectedTracks(DownloadItem.TrackType.TEXT, trackSelector.getAvailableTracks(DownloadItem.TrackType.TEXT))
    }

    fun playbackURL(entryID: String): String? {
       return cm?.getPlaybackURL(entryID)
//            .let {
//            return try {
//                URL(it)
//            } catch(e: Exception) {
//                Log.d("AMG", "Can't get playback URL for $entryID")
//                null
//            }
//        }
//        return null
    }

    public fun fetchAllStoredItems(): PlayKitDownloads {
        var downloads = PlayKitDownloads()
        allValidIDs.clear()
        downloads.completed = getItems(DownloadState.COMPLETED)
        downloads.new = getItems(DownloadState.NEW)
        downloads.metadataLoaded = getItems(DownloadState.INFO_LOADED)
        downloads.downloading = getItems(DownloadState.IN_PROGRESS)
        downloads.paused = getItems(DownloadState.PAUSED)
        downloads.failed = getItems(DownloadState.FAILED)
        return downloads
    }

    private fun getItems(forState: DownloadState): ArrayList<PlayKitDownloadItem> {
        var list: ArrayList<PlayKitDownloadItem> = ArrayList()
        cm?.let {cm ->
            try {
                for (item in cm.getDownloads(forState)){  //itemsByState(forState).forEach{item in
                    allValidIDs.add(item.itemId)
                    var myItem = PlayKitDownloadItem()
                    myItem.entryID = item.itemId
                    myItem.available = false
                    myItem.error = null
                    when (forState) {
                        DownloadState.NEW -> myItem.completedFraction = 0f
                        DownloadState.INFO_LOADED -> myItem.completedFraction = 0f
                        DownloadState.FAILED -> {
                            myItem.completedFraction = 0f
                            myItem.error = PlayKit2GoError.Unknown_Error
                        }
                        DownloadState.IN_PROGRESS, DownloadState.PAUSED -> myItem.completedFraction = item.estimatedCompletionPercent
                        DownloadState.COMPLETED -> {
                            myItem.completedFraction = 1f
                            myItem.available = true
                        }
                    }


                    list.add(myItem)
                }
            } catch (e: Exception){
                Log.e("AMG", "Error occurred compiling downloads - ${e.localizedMessage}")
            }
        }


            return list
        }
}