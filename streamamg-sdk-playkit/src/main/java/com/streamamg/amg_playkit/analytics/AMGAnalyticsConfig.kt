package com.streamamg.amg_playkit.analytics

import com.streamamg.amg_playkit.constants.AMGAnalyticsService

class AMGAnalyticsConfig() {
    var analyticsService: AMGAnalyticsService = AMGAnalyticsService.DISABLED
    var accountCode: String = ""
    var partnerID: Int = 0

    constructor(youboraAccountCode: String): this() {
        analyticsService = AMGAnalyticsService.YOUBORA
                accountCode = youboraAccountCode
    }

    constructor(amgAnalyticsPartnerID: Int) : this() {
        analyticsService = AMGAnalyticsService.AMGANALYTICS
                partnerID = amgAnalyticsPartnerID
    }
}