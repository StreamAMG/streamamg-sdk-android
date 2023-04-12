package com.streamamg.amg_playkit.models

import android.util.Log
import com.kaltura.playkit.PKMediaConfig
import com.kaltura.playkit.PKMediaEntry
import com.kaltura.playkit.PKMediaFormat
import com.kaltura.playkit.PKMediaSource
import com.kaltura.playkit.player.PKExternalSubtitle
import com.streamamg.amg_playkit.constants.AMGMediaType

class MediaItem(
    var serverURL: String,
    var partnerID: Int,
    var entryID: String,
    var ks: String?,
    var title: String?,
    var mediaType: AMGMediaType = AMGMediaType.VOD,
    var captionAsset: CaptionAssetElement? = null
) {
    var mediaConfig: PKMediaConfig = PKMediaConfig()

    init {
        val media = PKMediaEntry()
        media.id = entryID
        if (title != null) {
            media.name = title
        }
        captionAsset?.let {
            media.externalSubtitleList = externalSubtitlesList(it, media.duration)
        }
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

    private fun externalSubtitlesList(list: CaptionAssetElement?, duration: Long): MutableList<PKExternalSubtitle>? {
        if (list == null || list.objects.isNullOrEmpty()) {
            return null
        }

        var externalSubtitles = mutableListOf<PKExternalSubtitle>()

        for (value in list.objects) {
            if (serverURL.isNotEmpty() && value.id != null && value.language != null && value.languageCode != null && value.status != -1) {
                val url = "$serverURL/api_v3/index.php/service/caption_captionasset/action/serveWebVTT/captionAssetId/${value.id}/segmentIndex/-1/version/2/captions.vtt"
                val eSub = PKExternalSubtitle()
                eSub.language = value.language
                eSub.label = if (!value.label.isNullOrEmpty()) value.label else value.language
                eSub.url = url
                externalSubtitles.add(eSub)
            }
        }

        externalSubtitles = externalSubtitles.distinctBy { Pair(it.language, it.label) }.toMutableList()

        return externalSubtitles
    }
}