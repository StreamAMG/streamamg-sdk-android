package com.streamamg.amg_playkit.models

import com.streamamg.amg_playkit.constants.AMGControlPosition

data class AMGPlayKitStandardControlsConfigurationModel(
        var fadeInTogglesPausePlay: Boolean = false,
        var fadeInTime: Long = 0,
        var fadeOutTime: Long = 0,
        var fadeOutAfter: Long = 5000,
        var slideBarPosition: AMGControlPosition = AMGControlPosition.bottom,
        var trackTimeShowing: Boolean = false,
        var currentTimeShowing: Boolean = false,
        var skipForwardTime: Long = 5000,
        var skipBackwardTime: Long = 5000,
        var hideFullscreen: Boolean = false,
        var hideFullscreenOnFS: Boolean = false,
        var isLiveImage: Int = 0,
        var logoImage: Int = 0,
        var playImage: Int = 0,
        var pauseImage: Int = 0,
        var skipForwardImage: Int = 0,
        var skipBackwardImage: Int = 0,
        var fullScreenImage: Int = 0,
        var minimiseImage: Int = 0,
        var scrubBarLiveColour: Int = 0,
        var scrubBarVODColour: Int = 0,
        var bitrateSelector: Boolean = false
) {
}

/**
Builder class for the AMGPlayKitStandardControlsConfigurationModel structure

This is used when configuring the standard UI Control class for a more customisable look and feel
 */
class AMGControlBuilder {
    private var fadeInTogglesPausePlay = false
    private var fadeInTime: Long = 0
    private var fadeOutTime: Long = 0
    private var fadeOutAfter: Long = 5000
    private var slideBarPosition: AMGControlPosition = AMGControlPosition.bottom
    private var trackTimeShowing = false
    private var currentTimeShowing = false
    private var skipForwardTime: Long = 5000
    private var skipBackwardTime: Long = 5000
    private var hideFullscreen: Boolean = false
    private var hideFullscreenOnFS: Boolean = false
    private var isLiveImage: Int = 0
    private var logoImage: Int = 0
    private var playImage: Int = 0
    private var pauseImage: Int = 0
    private var skipForwardImage: Int = 0
    private var skipBackwardImage: Int = 0
    private var fullScreenImage: Int = 0
    private var minimiseImage: Int = 0
    private var scrubBarLiveColour: Int = 0
    private var scrubBarVODColour: Int = 0
    private var bitrateSelector: Boolean = false


    /**
    Not currently supported

    Toggle whether the current media toggles play state when the controls are made visible
     */
    fun setFadeInToggleOn(isOn: Boolean) = apply {
        fadeInTogglesPausePlay = isOn
    }

    fun setFadeInTime(time: Long) = apply {
        fadeInTime = time

    }

    /**
    Not currently supported

    Set the duration of the fade out animation of the controls in miliseconds
     */
    fun setFadeOutTime(time: Long) = apply {
        fadeOutTime = time

    }

    /**
    Specify the image to use for the play button
     */
    public fun playImage(image: Int) = apply {
        playImage = image
    }

    /**
    Specify the image to use for the pause button
     */
    public fun pauseImage(image: Int) = apply {
        pauseImage = image
    }

    /**
    Specify the image to use for the fullscreen button
     */
    public fun fullScreenImage(image: Int) = apply {
        fullScreenImage = image
    }

    /**
    Specify the image to use for the minimise button
     */
    public fun minimiseImage(image: Int) = apply {
        minimiseImage = image
    }

    /**
    Specify the image to use for the skip forwards button
     */
    public fun skipForwardImage(image: Int) = apply {
        skipForwardImage = image
    }

    /**
    Specify the image to use for the skip backward button
     */
    public fun skipBackwardImage(image: Int) = apply {
        skipBackwardImage = image
    }

    /**
    Specify the image to use for the 'is live'
     */
    public fun isLiveImage(image: Int) = apply {
        isLiveImage = image
    }

    /**
    Specify the image to use for the logo / watermark
     */
    public fun logoImage(image: Int) = apply {
        logoImage = image
    }

    /**
    Set the delay, in miliseconds, of the inactivity timer before hiding the controls
     */
    fun setHideDelay(time: Long) = apply {
        fadeOutAfter = time

    }

    /**
    Toggle the visibility of the current track time
    If the current play time toggle is on, this will display the start as 00:00 and the end as the duration of the media
    If the current play time toggle is off, this will display the start as the CURRENT time and the end as the time remaining
     */
    fun setTrackTimeShowing(isOn: Boolean) = apply {
        trackTimeShowing = isOn

    }

    /**
    Set the time skipped for backward and forward skip in milliseconds
     */
    public fun setSkipTime(time: Long) = apply {
        skipForwardTime = time
        skipBackwardTime = time
    }

    /**
    Set the time skipped for forward skip in milliseconds
     */
    public fun setSkipForwardTime(time: Long) = apply {
        skipForwardTime = time
    }

    /**
    Toggle the visibility of the bitrate selector
     */
    public fun setBitrateSelector(isOn: Boolean) = apply {
        bitrateSelector = isOn
    }

    /**
    Set the time skipped for backward skip in milliseconds
     */
    public fun setSkipBackwardTime(time: Long) = apply {
        skipBackwardTime = time
    }

    public fun hideMinimiseButton() = apply {
        hideFullscreenOnFS = true
    }

    public fun hideFullScreenButton() = apply {
        hideFullscreen = true
    }

    public fun scrubBarColour(colour: Int) = apply {
        scrubBarLiveColour = colour
        scrubBarVODColour = colour
    }

    public fun scrubBarLiveColour(colour: Int) = apply {
        scrubBarLiveColour = colour
    }

    public fun scrubBarVODColour(colour: Int) = apply {
        scrubBarVODColour = colour
    }

    /**
    Returns a complete and valid AMGPlayKitStandardControlsConfigurationModel
     */
    fun build(): AMGPlayKitStandardControlsConfigurationModel {
        return AMGPlayKitStandardControlsConfigurationModel(fadeInTogglesPausePlay, fadeInTime, fadeOutTime, fadeOutAfter, slideBarPosition, trackTimeShowing, currentTimeShowing, skipForwardTime, skipBackwardTime, hideFullscreen, hideFullscreenOnFS, isLiveImage, logoImage, playImage, pauseImage, skipForwardImage, skipBackwardImage, fullScreenImage, minimiseImage, scrubBarLiveColour, scrubBarVODColour, bitrateSelector)
    }

    //

}
