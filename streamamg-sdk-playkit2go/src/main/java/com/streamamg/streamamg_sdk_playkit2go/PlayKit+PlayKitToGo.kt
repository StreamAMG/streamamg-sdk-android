package com.streamamg.streamamg_sdk_playkit2go

import com.streamamg.amg_playkit.AMGPlayKit

fun AMGPlayKit.loadPlayKit2GoMedia(entryID: String, title: String? = null): Boolean{
    PlayKit2Go.getInstance().playbackURL(entryID)?.let{
        loadLocalMedia(entryID, it, title)
    }

    return false
}

