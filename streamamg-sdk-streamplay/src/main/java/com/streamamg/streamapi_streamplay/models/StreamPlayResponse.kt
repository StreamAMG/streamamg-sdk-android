package com.streamamg.streamapi_streamplay.models

import android.util.Log
import com.google.gson.annotations.SerializedName
import com.streamamg.streamapi_core.models.BaseStreamResponse
import com.streamamg.streamapi_streamplay.services.logErrorSP
import com.streamamg.streamapi_streamplay.services.logSP
import kotlin.collections.ArrayList

/**
 * Model returned from a valid, successful call by a StreamPlayRequest
 */
data class StreamPlayResponse (
        val fixtures: ArrayList<FixturesModel>

): BaseStreamResponse() {

        /**
         * Logs a list of all returned fixture manes to the console - debugging method only
         *
         * Core should be configured to have logging enabled
         */
        fun logFixtures(){
                logSP("SP --------------------------------------")
                for (fixture: FixturesModel in fixtures){
                        Log.d("StreamPlay", fixture.name ?: "")
                }
        }
}

/**
 * A single fixture returned by the StreamPlay API
 */
data class FixturesModel(
        var id: Int?,
        var type: String?,
        var partnerId: Int?,
        var featured: Boolean?,
        var name: String?,
        var description: String?,
        var startDate: String?,
        var endDate: String?,
        var createdAt: String?,
        var updatedAt: String?,
        var videoDuration: Int?,
        var externalIds: ExternalIDModel?,
        var season: FixtureDetailModel?,
        var competition: FixtureDetailModel?,
        var homeTeam: FixtureDetailModel?,
        var awayTeam: FixtureDetailModel?,
        var stadium: FixtureDetailModel?,
        var mediaData: ArrayList<ScheduleMediaDataModel> = ArrayList(),
        var thumbnail: String?,
        var thumbnailFlavors: FixtureThumbnailFlavorsModel?
) {

}

/**
 * Any external IDs that tie with this fixture
 */
data class ExternalIDModel(
    var optaFixtureId: String?,
    var paFixtureId: String?,
    var sportsradarFixtureId: String?
)

/**
 * A model that can house information about a specific item in a fixture (Team details, stadium details, etc)
 */
data class FixtureDetailModel(
        var id: Int?,
        var name: String?,
        var logo: String?,
        var logoFlavours: FixtureDetailLogoFlavorsModel?
)

/**
 * URLs for images held by the details in different resolutions
 */
data class FixtureDetailLogoFlavorsModel(
        @SerializedName("50")
        var logo50: String?,
        @SerializedName("100")
        var logo100: String?,
        @SerializedName("200")
        var logo200: String?,
        @SerializedName("300")
        var logo300: String?,
        var source: String?
)

/**
 * URLs for the main fixture thumbnail image in different resolutions
 */
data class FixtureThumbnailFlavorsModel(
        @SerializedName("250")
        var logo250: String?,
        @SerializedName("640")
        var logo640: String?,
        @SerializedName("1024")
        var logo1024: String?,
        @SerializedName("1920")
        var logo1920: String?,
        var source: String?
)

/**
 * Media data model - contains information regarding any associated media
 */
data class ScheduleMediaDataModel (
    var mediaType: String?,
    var entryId: String?,
    var isLiveUrl: String?,
    var isLiveTime: Long?,
    var thumbnailUrl: String?,
    var drm: Boolean?
)