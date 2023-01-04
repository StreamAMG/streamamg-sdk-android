package com.streamamg.amg_playkit.models

enum class TrackType {
    VIDEO, AUDIO, TEXT, IMAGE
}

class MediaTrack(
    var uniqueId: String,
    var type: TrackType,
    var language: String? = null,
    var label: String? = null,
    var mimeType: String? = null,
    var codecName: String? = null,
    var bitrate: Long? = null,
    var width: Int? = null,
    var height: Int? = null,
    var channelCount: Int? = null,
    var duration: Long? = null,
    var url: String? = null,
    var cols: Int? = null,
    var rows: Int? = null
)