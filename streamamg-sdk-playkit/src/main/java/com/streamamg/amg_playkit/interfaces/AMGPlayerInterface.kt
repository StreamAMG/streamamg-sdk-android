package com.streamamg.amg_playkit.interfaces

import com.streamamg.amg_playkit.constants.AMGPlayKitPlayState
import com.streamamg.amg_playkit.models.MediaTrack
import com.streamamg.amg_playkit.playkitExtensions.FlavorAsset

/**
This interface handles calls from UI controls , including play state and playhead position
 */
interface AMGPlayerInterface {
    fun play()
    fun pause()
    fun scrub(position: Long)
    fun skipForward()
    fun skipBackward()
    fun setControlInterface(controls: AMGControlInterface)
    fun cancelTimer()
    fun startControlVisibilityTimer()
    fun playState(): AMGPlayKitPlayState
    fun swapOrientation()
    fun goLive()
    fun setMaximumBitrate(bitrate: FlavorAsset?)
    fun setTrack(track: MediaTrack)
}