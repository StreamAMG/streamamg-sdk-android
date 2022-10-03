package com.streamamg.amg_playkit.interfaces

import com.streamamg.amg_playkit.models.AMGPlayKitError
import com.streamamg.amg_playkit.models.AMGPlayKitState
import com.streamamg.amg_playkit.playkitExtensions.FlavorAsset

public interface AMGPlayKitListener {
    fun playEventOccurred(state: AMGPlayKitState){}
    fun stopEventOccurred(state: AMGPlayKitState){}
    fun loadChangeStateOccurred(state: AMGPlayKitState){}
    fun durationChangeOccurred(state: AMGPlayKitState){}
 //  fun advertEventOccurred(state: AMGPlayKitState){}
    fun errorOccurred(error: AMGPlayKitError){}
    fun bitrateChangeOccurred(list: List<FlavorAsset>?){}
}