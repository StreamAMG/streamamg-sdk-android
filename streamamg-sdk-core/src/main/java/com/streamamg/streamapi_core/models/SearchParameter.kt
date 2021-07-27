package com.streamamg.streamapi_core.models

import com.streamamg.streamapi_core.constants.StreamAMGQueryType

class SearchParameter(
        val searchType: StreamAMGQueryType = StreamAMGQueryType.EQUALS,
        val target: String,
        val query: String,
        val typeDescription: String = "",
        val queryType: Int = -1
)