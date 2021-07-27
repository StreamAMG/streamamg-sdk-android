package com.streamamg.amg_playkit.analytics.timechunks

class ChunkBag {
    var chunks: ArrayList<TimeChunk> = ArrayList()

    fun clear() {
        chunks.clear()
    }

    fun add(timeChunk: TimeChunk) {
        chunks.add(timeChunk)
    }

    private fun combinedChunks(currentChunk: TimeChunk?): ArrayList<TimeChunk> {
        if (chunks.isEmpty() && currentChunk == null) {
            return chunks
        }
        val combinedChunks: ArrayList<TimeChunk> = ArrayList()
        val remainingChunks: ArrayList<TimeChunk> = ArrayList()
        var sortedChunks = sortedChunks(currentChunk)
        while (sortedChunks.isNotEmpty()) {
            var currentChunk = sortedChunks.removeAt(0)
            for (chunk in sortedChunks) {
                val newChunk = currentChunk.combine(chunk)
                if (newChunk == null) {
                    remainingChunks.add(chunk)
                } else {
                    currentChunk = newChunk
                }
            }
            combinedChunks.add(currentChunk)
            sortedChunks = ArrayList(remainingChunks)
            remainingChunks.clear()
        }
        return combinedChunks
    }

    fun totalDuration(currentChunk: TimeChunk? = null): Long {
        if (chunks.isEmpty()){
            currentChunk?.let {
                return it.duration()
            }
            return 0
        }
        var chunkTime = chunks.map { x -> x.duration() }.reduce { acc, timeChunk -> acc + timeChunk }
        currentChunk?.let {
            chunkTime += it.duration()
        }
        return chunkTime
    }

    private fun sortedChunks(currentChunk: TimeChunk? = null): ArrayList<TimeChunk> {
        var allChunks = ArrayList(chunks)
        currentChunk?.let{
            allChunks.add(it)
        }
        val sortedChunks = allChunks.sortedBy { x -> x.start }
        return ArrayList(sortedChunks)
    }

    fun combinedDuration(currentChunk: TimeChunk? = null): Long {
        val combinedChunks = combinedChunks(currentChunk)
        if (combinedChunks.isEmpty()){
            return 0
        }
        return combinedChunks.map { x -> x.duration() }.reduce { acc, timeChunk -> acc + timeChunk }
    }
}