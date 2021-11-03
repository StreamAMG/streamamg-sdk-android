package com.streamamg.amg_playkit.analytics

import android.util.Log
import com.streamamg.amg_playkit.constants.AMGAnalyticsService

class AMGAnalyticsConfig() {
    var analyticsService: AMGAnalyticsService = AMGAnalyticsService.DISABLED
    var accountCode: String = ""
    var partnerID: Int = 0
    var userName: String? = null
    var youboraParameters: ArrayList<YouboraParameter> = ArrayList()

    constructor(youboraAccountCode: String): this() {
        analyticsService = AMGAnalyticsService.YOUBORA
                accountCode = youboraAccountCode
    }

    constructor(amgAnalyticsPartnerID: Int) : this() {
        analyticsService = AMGAnalyticsService.AMGANALYTICS
                partnerID = amgAnalyticsPartnerID
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
                Log.d("AMG_YOUBORA","ID $id already exists")
            } else {
                youboraParametersObject.add(YouboraParameter(id, value))
            }
            } else {
            Log.d("AMG_YOUBORA","ID $id is out of range")
            }
        }

        fun build(): AMGAnalyticsConfig {
            if (accountCodeObject.isBlank()){
                Log.d("AMG_YOUBORA","Creating Youbora service with no account code")
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

        fun partnerID(id: Int) = apply {
            this.partnerObject = id
        }

        fun build(): AMGAnalyticsConfig {
            if (partnerObject == 0){
                Log.d("AMG_ANALYTICS","Creating AMG service with no PartnerID")
            }
            val service = AMGAnalyticsConfig(partnerObject)
            return service

        }
    }


}