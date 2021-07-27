package com.streamamg.amg_playkit.analytics

import android.util.LongSparseArray
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.plugins.kava.AverageBitrateCounter

class AMGAverageBitrateCounter {
    private val log = PKLog.get(AverageBitrateCounter::class.java.simpleName)

    private var shouldCount = false
    private var currentTrackBitrate: Long = -1
    private var currentTrackStartTimestamp: Long = 0

    private val averageTrackPlaybackDuration = LongSparseArray<Long>()

    /**
     * Calculate average bitrate for the entire media session.
     *
     * @param totalPlaytimeSum - total amount of the player being in active playback mode.
     * @return - average bitrate.
     */
    fun getAverageBitrate(totalPlaytimeSum: Long): Long {
        updateBitratePlayTime()
        var bitrate: Long
        var playTime: Long?
        var averageBitrate: Long = 0
        for (i in 0 until averageTrackPlaybackDuration.size()) {
            bitrate = averageTrackPlaybackDuration.keyAt(i)
            playTime = averageTrackPlaybackDuration[bitrate]
            if (playTime == null || totalPlaytimeSum == 0L) {
                continue
            }
            averageBitrate += bitrate * playTime / totalPlaytimeSum
        }
        return averageBitrate
    }

    private fun updateBitratePlayTime() {
        //We are not counting adaptive bitrate(0) selection as average.
        if (currentTrackBitrate == 0L) {
            return
        }
        val currentTimeStamp = System.currentTimeMillis()
        val playedTime = currentTimeStamp - currentTrackStartTimestamp

        //When it is first time that this bitrate is was selected we add it to the averageTrackPlaybackDuration
        //with the playedTime value and bitrate as key.
        var totalAveragePlayedTime = averageTrackPlaybackDuration[currentTrackBitrate]
        if (totalAveragePlayedTime == null) {
            averageTrackPlaybackDuration.put(currentTrackBitrate, playedTime)
        } else {
            // Otherwise we will get the existing value and add the last played one.
            //After that save it to averageTrackPlaybackDuration.
            totalAveragePlayedTime += playedTime
            averageTrackPlaybackDuration.put(currentTrackBitrate, totalAveragePlayedTime)
        }
        currentTrackStartTimestamp = currentTimeStamp
    }

    fun resumeCounting() {
        currentTrackStartTimestamp = System.currentTimeMillis()
        shouldCount = true
    }

    fun pauseCounting() {
        updateBitratePlayTime()
        shouldCount = false
    }

    fun setBitrate(bitrate: Long) {
        if (shouldCount) {
            updateBitratePlayTime()
        }
        currentTrackBitrate = bitrate
    }

    fun reset() {
        averageTrackPlaybackDuration.clear()
    }
}