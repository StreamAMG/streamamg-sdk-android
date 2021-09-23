package com.streamamg.amg_playkit.models

import android.util.Log
import com.kaltura.playkit.PKMediaConfig
import com.kaltura.playkit.PKMediaEntry
import com.kaltura.playkit.PKMediaFormat
import com.kaltura.playkit.PKMediaSource
import com.streamamg.amg_playkit.constants.AMGMediaType

class MediaItem(
    var serverURL: String,
    var partnerID: Int,
    var entryID: String,
    var ks: String?,
    var mediaType: AMGMediaType = AMGMediaType.VOD
) {
    var mediaConfig: PKMediaConfig = PKMediaConfig()

    init {
        val media = PKMediaEntry()
        media.id = entryID
        var sources = PKMediaSource()
        sources.id = entryID
        sources.url =
            "$serverURL/p/$partnerID/sp/${partnerID}00/playManifest/entryId/$entryID/format/applehttp/protocol/https/a.m3u8"
        ks?.let {
            sources.url += "?ks=$it"
        }
        sources.mediaFormat = PKMediaFormat.hls;
        media.sources = listOf(sources)
        when (mediaType) {
            AMGMediaType.Live, AMGMediaType.Live_Audio -> media.mediaType =
                PKMediaEntry.MediaEntryType.DvrLive
            else -> media.mediaType = PKMediaEntry.MediaEntryType.Vod
        }
        mediaConfig.mediaEntry = media
    }
}