package com.streamamg.amg_playkit.models

data class AMGPlayKitState(
        var state: AMGPlayerState,
//        var stateHasChanged: Boolean = false,
        var duration: Long = -1
)

data class AMGPlayKitError(
        var state: AMGPlayerState = AMGPlayerState.Error,
        var errorCode: Int = 0,
        var errorMessage: String = ""
)


public enum class AMGPlayerState {
    Stopped, Playing, Error, Ad_Started, Ad_Ended, Ended, Loaded, Play, Stop, Pause, Idle, Loading, Buffering, Ready
}

public enum class AMGPlayerError(val errorCode: Int) {
    SOURCE_ERROR(7000), RENDERER_ERROR(7001), UNEXPECTED(7002), SOURCE_SELECTION_FAILED(7003), FAILED_TO_INITIALIZE_PLAYER(7004), DRM_ERROR(7005), TRACK_SELECTION_FAILED(7006), LOAD_ERROR(7007), OUT_OF_MEMORY(7008), REMOTE_COMPONENT_ERROR(7009), TIMEOUT(7010)
}
