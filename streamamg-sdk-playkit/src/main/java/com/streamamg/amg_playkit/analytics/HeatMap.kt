package com.streamamg.amg_playkit.analytics

import android.util.Log
import com.streamamg.amg_playkit.analytics.timechunks.ChunkBag
import com.streamamg.amg_playkit.analytics.timechunks.OpenTimeChunk
import com.streamamg.amg_playkit.analytics.timechunks.TimeChunk

class HeatMap {
    private val heatMapSize: Int = 20
    var heatMap = IntArray(heatMapSize)
    var sectionSpread = LongArray(heatMapSize)
    var sectionLength: Long = 0
    var duration: Long = 0
    var currentSection = 0
    var sectionStart: Long = 0
    var sectionEnd: Long = 0
    var currentTime: Long = 0

    var chunkBag = ChunkBag()
    var currentChunk: OpenTimeChunk? = null

    fun resetHeatMap(duration: Long) {
        heatMap = IntArray(heatMapSize)
        currentSection = 0
        sectionStart = 0
        sectionEnd = 0
        this.duration = duration
        if (duration > 0) {
            sectionLength = duration / heatMapSize.toLong()
            var count: Long = 0
            for (a in 0 until heatMapSize) {
                sectionSpread[a] = count
                count += sectionLength
            }
        } else {
            sectionLength = 0
            sectionSpread = LongArray(heatMapSize)
        }
        resetChunkBag()
    }

    fun updateHeatMap(currentTime: Long) {
        if (duration == 0L) {
            return
        }
        this.currentTime = currentTime
        if (currentTime < sectionStart || currentTime > sectionEnd) {
            markNewSection(currentTime)
        }
    }

    private fun markNewSection(currentTime: Long) {
        currentSection = 0
        for (time in sectionSpread) {
            if (currentTime > time + sectionLength) {
                currentSection += 1
            }
        }
        if (currentSection >= sectionSpread.size){
            currentSection = sectionSpread.size - 1
        }
        sectionStart = sectionSpread[currentSection]
        sectionEnd = if (currentSection < heatMapSize - 1) {
            sectionSpread[currentSection + 1]
        } else {
            duration
        }
        heatMap[currentSection] = 1
    }

    fun report(): String {
        return heatMap.joinToString { it.toString() }.replace(" ", "")
    }

    private fun resetChunkBag() {
        chunkBag.clear()
        currentChunk = OpenTimeChunk(0)
    }

    private fun closeChunk(at: Long){
        currentChunk?.close(at)?.let {timeChunk -> {
            chunkBag.add(timeChunk)
        }
            currentChunk = null
        }
    }

    private fun createChunk(at: Long){
        currentChunk = OpenTimeChunk(at)
    }

    fun movePlayHeadManually(moveFrom: Long, moveTo: Long) {
        closeChunk(moveFrom)
        createChunk(moveTo)
    }

    fun totalPlayTime(inSeconds: Boolean = true): Long{
        val total = chunkBag.totalDuration(currentChunk?.close(currentTime))
        return when (inSeconds){
           true -> total / 1000
            false -> total
        }
    }

    fun uniquePlayTime(inSeconds: Boolean = true): Long{
        val total =  chunkBag.combinedDuration(currentChunk?.close(currentTime))
        return when (inSeconds){
            true -> total / 1000
            false -> total
        }
    }

    fun markCompleted() {
        closeChunk(duration)
    }

}