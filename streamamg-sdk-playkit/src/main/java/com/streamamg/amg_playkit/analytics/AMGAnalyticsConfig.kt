package com.streamamg.amg_playkit.analytics

import android.util.Log
import com.streamamg.amg_playkit.constants.AMGAnalyticsService

class AMGAnalyticsConfig() {

    var analyticsService: AMGAnalyticsService = AMGAnalyticsService.DISABLED
    var accountCode: String = ""
    var partnerID: Int = 0
    var configID: Int = 0
    var userName: String? = null
    var youboraParameters: ArrayList<YouboraParameter> = ArrayList()

    constructor(youboraAccountCode: String): this() {
        analyticsService = AMGAnalyticsService.YOUBORA
        accountCode = youboraAccountCode
    }

    constructor(amgAnalyticsPartnerID: Int, amgAnalyticsConfigID: Int?) : this() {
        analyticsService = AMGAnalyticsService.AMGANALYTICS
        partnerID = amgAnalyticsPartnerID
        configID = amgAnalyticsConfigID ?: 0
    }

    fun updateYouboraParameter(id: Int, value: String) {
        if (analyticsService == AMGAnalyticsService.YOUBORA){
            youboraParameters.find { x -> x.id == id }?.let {
                it.value = value
                return
            }
            youboraParameters.add(YouboraParameter(id, value))
        }
    }

    class YouboraService(
    ) {
        private var accountCodeObject: String = ""
        private var userNameObject: String? = null
        private var youboraParametersObject: ArrayList<YouboraParameter> = ArrayList()

        fun accountCode(code: String) = apply {
            this.accountCodeObject = code
        }

        fun userName(name: String) = apply {
            this.userNameObject = name
        }

        fun parameter(id: Int, value: String) = apply {
            if (id in 1..20) {
                if (youboraParametersObject.find { x -> x.id == id } != null){
            } else {
                youboraParametersObject.add(YouboraParameter(id, value))
            }
            }
        }

        fun build(): AMGAnalyticsConfig {
            if (accountCodeObject.isBlank()){
                Log.e("AMGPlayKit","Creating Youbora service with no account code")
            }
            val service = AMGAnalyticsConfig(accountCodeObject)
            service.userName = userNameObject
            service.youboraParameters = youboraParametersObject
            return service

        }
    }


    class AMGService(
    ) {
        private var partnerObject: Int = 0
        private var configObject: Int = 0

        fun partnerID(id: Int) = apply {
            this.partnerObject = id
        }

        fun configID(id: Int) = apply {
            this.configObject = id
        }

        fun build(): AMGAnalyticsConfig {
            if (partnerObject == 0) {
                Log.e("AMGPlayKit", "Creating AMG service with no PartnerID")
            }
            if (configObject == 0) {
                Log.e("AMGPlayKit", "Creating AMG service with no ConfigID")
            }
            return AMGAnalyticsConfig(partnerObject, configObject)

        }
    }


}