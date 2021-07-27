package com.streamamg.streamapi_core.models

open class BaseStreamResponse (
    var success: Boolean? = null,
    val total: Int = 0,
    val limit: Int = 1,
    val offset: Int = 1,
    var currentPage: Int = 0
){
    open fun fetchTotal(): String {
        return total.toString()
    }

    open fun fetchLimit(): String {
        return limit.toString()
    }

    open fun nextPage(): Int{
        var page = offset + limit
        if (page >= total){
            page = offset
        }
        return page
    }

    open fun previousPage(): Int{
        var page = offset - limit
        if (page <= 0){
            page = 0
        }
        return page
    }

    open fun fetchRetrieved(): String {
        return "X"
    }

    open fun fetchPageNumber(): String {
        if (limit > 0) {
            return "${(offset / limit) + 1}"
        }
        return "-"
    }

    open fun fetchPageTotal(): String {
        var pageTotal = total / limit
        if (pageTotal * limit < total){
            pageTotal += 1
        }
        return "$pageTotal"
    }

}