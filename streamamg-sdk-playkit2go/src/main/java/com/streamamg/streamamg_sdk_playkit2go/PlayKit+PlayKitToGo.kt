package com.streamamg.streamamg_sdk_playkit2go

import com.streamamg.amg_playkit.AMGPlayKit

fun AMGPlayKit.loadPlayKit2GoMedia(entryID: String): Boolean{
    val downloadSuccessful = false
    PlayKit2Go.getInstance().playbackURL(entryID)?.let{
        loadMedia(entryID, it)
    }

    return false
}

