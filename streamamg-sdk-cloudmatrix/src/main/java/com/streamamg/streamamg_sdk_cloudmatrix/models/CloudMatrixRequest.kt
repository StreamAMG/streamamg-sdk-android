package com.streamamg.streamamg_sdk_cloudmatrix.models

import com.streamamg.streamamg_sdk_cloudmatrix.constants.CloudMatrixFunction
import com.streamamg.streamamg_sdk_cloudmatrix.constants.CloudMatrixInternalPreferences
import com.streamamg.streamamg_sdk_cloudmatrix.constants.CloudMatrixQueryType
import com.streamamg.streamamg_sdk_cloudmatrix.services.logCM
import com.streamamg.streamamg_sdk_cloudmatrix.services.logErrorCM
import com.streamamg.streamamg_sdk_cloudmatrix.services.logNetworkCM
import com.streamamg.streamapi_core.StreamAMGSDK
import com.streamamg.streamapi_core.StreamPreferences
import com.streamamg.streamapi_core.constants.StreamAMGQueryType
import com.streamamg.streamapi_core.constants.StreamAPIEnvironment
import com.streamamg.streamapi_core.models.SearchParameter
import java.lang.StringBuilder

/**
 * Model that holds the payload of a request to the CloudMatrix API
 * It is preferred that a builder class is used to ensure all relevant information is included
 *
 * @param apiFunction 'CloudMatrixFunction' enum that determines what type of call is made - defaults to 'FEED'
 * @param event The ID of the event being queried
 * @param params A list of 'SearchParameter' objects containing valid queries
 * @param url A URL that should be directly queried
 * @param paginateBy A requested number of items per page to be returned - default is 200
 */
class CloudMatrixRequest(
    private val apiFunction: CloudMatrixFunction = CloudMatrixFunction.FEED,
    var event: String? = null,
    var params: ArrayList<SearchParameter> = ArrayList(),
    var url: String? = null,
    var paginateBy: Int = 0
) {

    private lateinit var cmSetup: CloudMatrixSetupModel
    var currentPage = 0

    internal fun createURL(): String {
        url?.let { guaranteedURL ->
            if (params.isEmpty()) {
                val query = paginatedStaticURL(guaranteedURL)
                logNetworkCM("Query = $query")
                return query
            }
            var query = ""
            query = when {
                guaranteedURL.contains("?", false) -> {
                    amendedSearchURL(guaranteedURL)
                }
                guaranteedURL.contains("search") -> {
                    "$guaranteedURL${parmeters()}"
                }
                else -> {
                    "$guaranteedURL/$apiFunction/${specificEvent()}${parmeters()}"
                }
            }
            logNetworkCM( "Query =$query")
            return query
        }
        return if (!this::cmSetup.isInitialized) {
            logErrorCM("StreamSDK CloudMatrix is not initialised.")
            ""
        } else if (cmSetup.userID.isNotEmpty()) {
            val query =
                "${baseURL()}/${cmSetup.version}/${cmSetup.userID}/${cmSetup.key}/${cmSetup.language}/$apiFunction/${specificEvent()}${parmeters()}"
            logNetworkCM("Query = $query")
            query
        } else {
            logErrorCM("StreamSDK CloudMatrix is not initialised.")
            ""
        }

    }

    private fun parmeters(): String {
        return when (apiFunction) {
            CloudMatrixFunction.SEARCH -> searchParameters()
            else -> ""
        }
    }

    private fun paginatedStaticURL(guaranteedURL: String): String {
        var returnURL = guaranteedURL
        if (!returnURL.contains("?")) {
            if (returnURL.endsWith("sections")) {
                returnURL += "/search"
            } else if (returnURL.endsWith("sections/")) {
                returnURL += "search"
            }
            returnURL += "?"
        } else {
            returnURL += "&"
        }
        return returnURL + pagination()
    }

    private fun amendedSearchURL(guaranteedURL: String): String{
        var returnURL = StringBuilder(guaranteedURL)
        var parameters = ""
        for(parameter in params) {
            if (parameter.query.isNotEmpty() || parameter.searchType == StreamAMGQueryType.EXISTS) {
                parameters = "$parameters%20AND%20"
                parameters = "$parameters${parameterString(parameter)}"
            }
        }
            if (returnURL.contains(")))")){
                val index = returnURL.indexOf(")))")
                returnURL = returnURL.insert(index+1, parameters)
            } else if (returnURL.contains("))")){
                val index = returnURL.indexOf(")))")
                returnURL = returnURL.insert(index, parameters)
            }
        logCM(returnURL.toString())
        returnURL.append("&")
        returnURL.append(pagination())
        return returnURL.toString()
    }

    private fun pagination(): String {
        var pagination = "pageIndex=$currentPage"
        if (paginateBy > 0) {
            pagination += "&pageSize=$paginateBy"
        }
        return pagination
    }

    private fun searchParameters(): String {
        if (params.isEmpty()) {
            return "?${pagination()}"
        }
        var parameters = ""
        for (parameter: SearchParameter in params) {
            if (parameters.isNotEmpty()) {
                parameters += "%20AND%20"
            }
            parameters += parameterString(parameter)
        }
        val param = "?query=($parameters)&${pagination()}"
        logCM(param)
        return param
    }

    private fun parameterString(parameter: SearchParameter): String {
        return when (parameter.searchType) {
            StreamAMGQueryType.EXISTS -> "_exists_:${parameter.target}"
            else -> "${parameter.target}:${parameter.searchType.operator}${parameter.query}"
        }

    }

    /**
     * Adds a parameter to the current params list
     *
     * @param parameter A 'SearchParameter' object containing a valid query
     */
    fun addSearch(parameter: SearchParameter) {
        params.add(parameter)
    }

    private fun specificEvent(): String {
        return when (apiFunction) {
            CloudMatrixFunction.FEED -> "$event/sections/"
            else -> ""
        }
    }

    private fun baseURL(): String {
        return when (StreamAMGSDK.getInstance().environment) {
            StreamAPIEnvironment.PRODUCTION -> cmSetup.url
            else -> cmSetup.debugURL
        }
    }

    fun updateWith(setupModel: CloudMatrixSetupModel?) {
        setupModel?.let {
            cmSetup = it
        }
    }

    /**
     * Builder class for creating 'FEED' queries
     * It is preferred that a builder class is used to ensure all relevant information is included
     */
    class FeedBuilder {
        private var event: String? = null
        private var pagination: Int = 0
        private var url: String? = null

        /**
         * Adds an event ID to the query - should not be used with 'url'
         */
        fun event(event: String) = apply {
            this.event = event
        }

        /**
         * Adds a fully formed URL to the query - should not be used with 'event'
         */
        fun url(url: String) = apply {
            this.url = url
        }

        /**
         * A requested number of items per page to be returned - default is 200
         */
        fun paginateBy(paginateBy: Int) = apply {
            this.pagination = paginateBy
        }

        /**
         * Returns a valid CloudMatrixRequest
         */
        fun build() = CloudMatrixRequest(event = event, paginateBy = pagination, url = url)
    }

    /**
     * Builder class for creating 'SEARCH' queries
     * It is preferred that a builder class is used to ensure all relevant information is included
     */
    data class SearchBuilder(var params: ArrayList<SearchParameter> = ArrayList()) {
        private var pagination: Int = 0
        private var workableURL: String? = null


        /**
         * Sets a URL (either a feed or search URL) to be used as the base URL for this search.
         *
         * @param url A valid CloudMatrix URL that includes 'feed' or 'search'
         */
        fun url(url: String) = apply {
            // Strip 'feed' info from URL
            if (url.contains("/feed")) {
                val index = url.indexOf("/feed")
                workableURL = url.substring(0, index)
            } else if (url.contains("/search")) {
                workableURL = url
            }
        }

        /**
         * Adds an 'equality' check to the query that returns only items in which this field contains a complete word or string that matches the query
         *
         * @param target A 'CloudMatrixQueryType' enum indicating the field to query
         * @param query The actual data to query
         */
        fun isEqualTo(target: CloudMatrixQueryType, query: String) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.EQUALS, target.query, query))
        }

        /**
         * Adds a 'comparative' check to the query that returns only items in which this field contains a value (numerical or alphabetically) greater than the query
         *
         * @param target A 'CloudMatrixQueryType' enum indicating the field to query
         * @param query The actual data to query
         */
        fun isGreaterThan(target: CloudMatrixQueryType, query: String) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.GREATERTHAN, target.query, query))
        }

        /**
         * Adds a 'comparative' check to the query that returns only items in which this field contains a value (numerical or alphabetically) greater than or equal to the query
         *
         * @param target A 'CloudMatrixQueryType' enum indicating the field to query
         * @param query The actual data to query
         */
        fun isGreaterThanOrEqualTo(target: CloudMatrixQueryType, query: String) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.GREATERTHANOREQUALTO, target.query, query))
        }

        /**
         * Adds a 'comparative' check to the query that returns only items in which this field contains a value (numerical or alphabetically) less than the query
         *
         * @param target A 'CloudMatrixQueryType' enum indicating the field to query
         * @param query The actual data to query
         */
        fun isLessThan(target: CloudMatrixQueryType, query: String) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.LESSTHAN, target.query, query))
        }

        /**
         * Adds a 'comparative' check to the query that returns only items in which this field contains a value (numerical or alphabetically) less than or equal to the query
         *
         * @param target A 'CloudMatrixQueryType' enum indicating the field to query
         * @param query The actual data to query
         */
        fun isLessThanOrEqualTo(target: CloudMatrixQueryType, query: String) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.LESSTHANOREQUALTO, target.query, query))
        }

        /**
         * Adds an 'equality' check to the query that returns only items in which this field contains a number equal to the query
         *
         * @param target A 'CloudMatrixQueryType' enum indicating the field to query
         * @param query The actual data to query
         */
        fun isEqualTo(target: CloudMatrixQueryType, query: Number) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.EQUALS, target.query, query.toString()))
        }

        /**
         * Adds a 'comparative' check to the query that returns only items in which this field contains a numerical value greater than the query
         *
         * @param target A 'CloudMatrixQueryType' enum indicating the field to query
         * @param query The actual data to query
         */
        fun isGreaterThan(target: CloudMatrixQueryType, query: Number) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.GREATERTHAN, target.query, query.toString()))
        }

        /**
         * Adds a 'comparative' check to the query that returns only items in which this field contains a numerical value greater than or equal to the query
         *
         * @param target A 'CloudMatrixQueryType' enum indicating the field to query
         * @param query The actual data to query
         */
        fun isGreaterThanOrEqualTo(target: CloudMatrixQueryType, query: Number) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.GREATERTHANOREQUALTO, target.query, query.toString()))
        }

        /**
         * Adds a 'comparative' check to the query that returns only items in which this field contains a numerical value less than the query
         *
         * @param target A 'CloudMatrixQueryType' enum indicating the field to query
         * @param query The actual data to query
         */
        fun isLessThan(target: CloudMatrixQueryType, query: Number) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.LESSTHAN, target.query, query.toString()))
        }

        /**
         * Adds a 'comparative' check to the query that returns only items in which this field contains a numerical value less than or equal to the query
         *
         * @param target A 'CloudMatrixQueryType' enum indicating the field to query
         * @param query The actual data to query
         */
        fun isLessThanOrEqualTo(target: CloudMatrixQueryType, query: Number) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.LESSTHANOREQUALTO, target.query, query.toString()))
        }

        /**
         * Adds a 'fuzzy' check to the query that returns only items in which this field where any part of the value contains the query
         *
         * @param target A 'CloudMatrixQueryType' enum indicating the field to query
         * @param query The actual data to query
         */
        fun isLike(target: CloudMatrixQueryType, query: String) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.FUZZY, target.query, "$query~1"))
        }

        /**
         * Adds a 'fuzzy' check to the query that returns only items in which this field where any word or continual string of the value starts with the query
         *
         * @param target A 'CloudMatrixQueryType' enum indicating the field to query
         * @param query The actual data to query
         */
        fun startsWith(target: CloudMatrixQueryType, query: String) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.EQUALS, target.query, "$query*"))
        }

        /**
         * Adds a 'fuzzy' check to the query that returns only items in which this field where any word or continual string of the value ends with the query
         *
         * @param target A 'CloudMatrixQueryType' enum indicating the field to query
         * @param query The actual data to query
         */
        fun endsWith(target: CloudMatrixQueryType, query: String) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.EQUALS, target.query, "*$query"))
        }

        /**
         * Adds a check to the query that returns only items in which this field exists
         *
         * @param target A 'CloudMatrixQueryType' enum indicating the field to query
         */
        fun exists(target: CloudMatrixQueryType) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.EXISTS, target.query, ""))
        }

        /**
         * Adds a boolean check to the query that returns only items in this field which are 'true'
         *
         * @param target A 'CloudMatrixQueryType' enum indicating the field to query
         */
        fun isTrue(target: CloudMatrixQueryType) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.EQUALS, target.query, "true"))
        }

        /**
         * Adds a boolean check to the query that returns only items where values in this field which are 'false'
         *
         * @param target A 'CloudMatrixQueryType' enum indicating the field to query
         */
        fun isFalse(target: CloudMatrixQueryType) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.EQUALS, target.query, "false"))
        }

        /**
         * Adds a 'containing' check to the query that returns only items where this array field contains the query
         *
         * @param target A 'CloudMatrixQueryType' enum indicating the field to query
         * @param query The actual data to query
         */
        fun contains(target: CloudMatrixQueryType, query: String) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.EQUALS, target.query, query))
        }

        /**
         * Adds an 'equality' check to the query that returns only items in which this field contains a complete word or string that matches the query
         *
         * @param target A String indicating the field to query
         * @param query The actual data to query
         */
        fun isEqualTo(target: String, query: String) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.EQUALS, target, query))
        }

        /**
         * Adds a 'comparative' check to the query that returns only items in which this field contains a value (numerical or alphabetically) greater than the query
         *
         * @param target A String indicating the field to query
         * @param query The actual data to query
         */
        fun isGreaterThan(target: String, query: String) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.GREATERTHAN, target, query))
        }

        /**
         * Adds a 'comparative' check to the query that returns only items in which this field contains a value (numerical or alphabetically) greater than or equal to the query
         *
         * @param target A String indicating the field to query
         * @param query The actual data to query
         */
        fun isGreaterThanOrEqualTo(target: String, query: String) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.GREATERTHANOREQUALTO, target, query))
        }

        /**
         * Adds a 'comparative' check to the query that returns only items in which this field contains a value (numerical or alphabetically) less than the query
         *
         * @param target A String indicating the field to query
         * @param query The actual data to query
         */
        fun isLessThan(target: String, query: String) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.LESSTHAN, target, query))
        }

        /**
         * Adds a 'comparative' check to the query that returns only items in which this field contains a value (numerical or alphabetically) less than or equal to the query
         *
         * @param target A String indicating the field to query
         * @param query The actual data to query
         */
        fun isLessThanOrEqualTo(target: String, query: String) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.LESSTHANOREQUALTO, target, query))
        }

        /**
         * Adds an 'equality' check to the query that returns only items in which this field contains a number equal to the query
         *
         * @param target A String indicating the field to query
         * @param query The actual data to query
         */
        fun isEqualTo(target: String, query: Number) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.EQUALS, target, query.toString()))
        }

        /**
         * Adds a 'comparative' check to the query that returns only items in which this field contains a numerical value greater than the query
         *
         * @param target A String indicating the field to query
         * @param query The actual data to query
         */
        fun isGreaterThan(target: String, query: Number) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.GREATERTHAN, target, query.toString()))
        }

        /**
         * Adds a 'comparative' check to the query that returns only items in which this field contains a numerical value greater than or equal to the query
         *
         * @param target A String indicating the field to query
         * @param query The actual data to query
         */
        fun isGreaterThanOrEqualTo(target: String, query: Number) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.GREATERTHANOREQUALTO, target, query.toString()))
        }

        /**
         * Adds a 'comparative' check to the query that returns only items in which this field contains a numerical value less than the query
         *
         * @param target A String indicating the field to query
         * @param query The actual data to query
         */
        fun isLessThan(target: String, query: Number) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.LESSTHAN, target, query.toString()))
        }

        /**
         * Adds a 'comparative' check to the query that returns only items in which this field contains a numerical value less than or equal to the query
         *
         * @param target A String indicating the field to query
         * @param query The actual data to query
         */
        fun isLessThanOrEqualTo(target: String, query: Number) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.LESSTHANOREQUALTO, target, query.toString()))
        }

        /**
         * Adds a 'fuzzy' check to the query that returns only items in which this field where any part of the value contains the query
         *
         * @param target A String indicating the field to query
         * @param query The actual data to query
         */
        fun isLike(target: String, query: String) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.FUZZY, target, "$query~1"))
        }

        /**
         * Adds a 'fuzzy' check to the query that returns only items in which this field where any word or continual string of the value starts with the query
         *
         * @param target A String indicating the field to query
         * @param query The actual data to query
         */
        fun startsWith(target: String, query: String) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.EQUALS, target, "$query*"))
        }

        /**
         * Adds a 'fuzzy' check to the query that returns only items in which this field where any word or continual string of the value starts with the query
         *
         * @param target A String indicating the field to query
         * @param query The actual data to query
         */
        fun endsWith(target: String, query: String) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.EQUALS, target, "*$query"))
        }

        /**
         * Adds a check to the query that returns only items in which this field exists
         *
         * @param target A String indicating the field to query
         */
        fun exists(target: String) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.EXISTS, target, ""))
        }

        /**
         * Adds a boolean check to the query that returns only items in this field which are 'true'
         *
         * @param target A String indicating the field to query
         */
        fun isTrue(target: String) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.EQUALS, target, "true"))
        }

        /**
         * Adds a boolean check to the query that returns only items in this field which are 'false'
         *
         * @param target A String indicating the field to query
         */
        fun isFalse(target: String) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.EQUALS, target, "false"))
        }

        /**
         * Adds a 'containing' check to the query that returns only items where this array field contains the query
         *
         * @param target A String indicating the field to query
         * @param query The actual data to query
         */
        fun contains(target: String, query: String) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.EQUALS, target, query))
        }

        /**
         * A requested number of items per page to be returned - default is 200
         */
        fun paginateBy(paginateBy: Int) = apply {
            this.pagination = paginateBy
        }

        /**
         * Returns a valid CloudMatrixRequest
         */
        fun build() = CloudMatrixRequest(CloudMatrixFunction.SEARCH, params = params, url = workableURL, paginateBy = pagination)
    }


}