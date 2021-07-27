package com.streamamg.amg_playkit

import com.streamamg.amg_playkit.analytics.timechunks.ChunkBag
import com.streamamg.amg_playkit.analytics.timechunks.TimeChunk
import org.junit.Assert
import org.junit.Test

class TimeChunkUnitTests {
    @Test
    fun time_chunk_duration_is_correct() {
        var timeChunk = TimeChunk(1,3)
        Assert.assertEquals(2, timeChunk.duration())
    }

    @Test
    fun chunk_bag_total_duration_is_correct(){
        var chunkBag = ChunkBag()
        chunkBag.add(TimeChunk(1,3))
        chunkBag.add(TimeChunk(4,7))
        chunkBag.add(TimeChunk(2,5))
        Assert.assertEquals(8, chunkBag.totalDuration())
    }

    @Test
    fun chunk_bag_combined_duration_is_correct(){
        var chunkBag = ChunkBag()
        chunkBag.add(TimeChunk(1,3))
        chunkBag.add(TimeChunk(4,7))
        chunkBag.add(TimeChunk(2,5))
        chunkBag.add(TimeChunk(10, 15))
        Assert.assertEquals(11, chunkBag.combinedDuration())
    }
}