package com.streamamg.amg_playkit.analytics.timechunks

class OpenTimeChunk(private val start: Long) {

    fun close(at: Long): TimeChunk? {
        if (at >= start) {
            return TimeChunk(start, at)
        }
        return null
    }

}