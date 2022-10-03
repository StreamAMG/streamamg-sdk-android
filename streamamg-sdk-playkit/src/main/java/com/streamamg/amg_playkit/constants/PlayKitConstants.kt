package com.streamamg.amg_playkit.constants

/**
An enum constant defenition to define the position of various UI Control elements in the standard controls
 */
enum class AMGControlPosition {
    top, bottom, left, right, topleft, topright, bottomleft, bottomright, centre
}

enum class AMGPlayKitPlayState {
    playing, paused, ended, idle
}

enum class SensorStateChangeActions {
    WATCH_FOR_LANDSCAPE_CHANGES, SWITCH_FROM_LANDSCAPE_TO_STANDARD, WATCH_FOR_PORTAIT_CHANGES, SWITCH_FROM_POTRAIT_TO_STANDARD
}

enum class AMGMediaType {
    Live, VOD, Audio, Live_Audio
}

enum class AMGRequestMethod {
    GET, POST
}

enum class  AMGMediaFormat {
    MP4, HLS
}

enum class AMGAnalyticsService {
    DISABLED, YOUBORA, AMGANALYTICS
}