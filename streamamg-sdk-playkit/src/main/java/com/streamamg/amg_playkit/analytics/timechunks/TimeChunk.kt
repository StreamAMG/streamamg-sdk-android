package com.streamamg.amg_playkit.analytics.timechunks

import kotlin.math.*

class TimeChunk(val start: Long, private val end: Long) {

    fun duration(): Long {
        return end - start
    }

    private fun intersectsWith(chunk: TimeChunk): Boolean{
       return ((chunk.start in (start + 1) until end) || (chunk.end in (start + 1) until end))
    }

    fun combine(chunk: TimeChunk): TimeChunk? {
        if (intersectsWith(chunk)){
            return TimeChunk(min(start, chunk.start), max(end, chunk.end))
        }
        return null
    }
}