package com.streamamg.amg_playkit.interfaces

/**
This interface handles call backs to the controls themselves, including play state and playhead position
 */
interface AMGControlInterface {
    fun play()
    fun pause()
    fun changePlayHead(position: Long)
    fun changeMediaLength(length: Long)
}