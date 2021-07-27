package com.streamamg.streamapi_streamplay.models

import com.streamamg.streamapi_core.StreamAMGSDK
import com.streamamg.streamapi_core.constants.StreamAMGQueryType
import com.streamamg.streamapi_core.constants.StreamAPIEnvironment
import com.streamamg.streamapi_core.models.SearchParameter
import com.streamamg.streamapi_streamplay.constants.StreamPlayQueryField
import com.streamamg.streamapi_streamplay.constants.StreamPlayQueryType
import com.streamamg.streamapi_streamplay.constants.StreamPlaySport
import com.streamamg.streamapi_streamplay.services.logErrorSP
import com.streamamg.streamapi_streamplay.services.logNetworkSP
import com.streamamg.streamapi_streamplay.services.logSP
/**
 * Model that holds the payload of a request to the StreamPlay API
 * It is preferred that a builder class is used to ensure all relevant information is included
 *
 * @param sport Array list of 'StreamPlaySport' enums the partner has access to
 * @param fixtureID The ID of the fixture being queried
 * @param partnerID The ID of the partner accessing the API
 * @param params A list of 'SearchParameter' objects containing valid queries
 * @param url A URL that should be directly queried
 * @param paginateBy A requested number of items per page to be returned - default is 20
 */
class StreamPlayRequest(
        val sport: ArrayList<StreamPlaySport>,
        var fixtureID: String? = null,
        var partnerID: String? = null,
        var params: ArrayList<SearchParameter> = ArrayList(),
        var url: String? = null,
        var paginateBy: Int = 0) {

    var currentOffset = 0

    internal fun createURL(): String {

       fixtureID?.let{fixture ->
           partnerID?.let{partner ->
               var query = pagination("${baseURL()}fixtures${getSingleSport()}/p/$partner${getFilters()}?q=(id:$fixture)")
               logNetworkSP("Query = $query")
               return query
           }
           logErrorSP("Partner ID must be specified for fixture searches")
           return ""
       }

       url?.let{guaranteedURL ->
           val query = pagination("$guaranteedURL${getFilters(guaranteedURL.contains("?"))}")
           logNetworkSP("Query = $query")
           return query
       }

       partnerID?.let{partner ->
           fixtureID?.let{fixture ->
                   var query = pagination("${baseURL()}fixtures${getSingleSport()}/p/$partner?q=(id:$fixture)")
                   logNetworkSP("Query = $query")
                   return query
           }
           var query = pagination("${baseURL()}fixtures${getSingleSport()}/p/$partner${getFilters()}")
           logNetworkSP("Query = $query")
           return query
       }

       logErrorSP("Criteria for constructing feed not met")

       return ""
    }

    private fun getSingleSport(): String{
        if (sport.size == 1){
            return "/${sport[0].value}"
        }
        return ""
    }

    private fun getFilters(amend: Boolean = false): String{
        if (params.isEmpty()) {
            return ""
        }
        var parameters = ""
        var media = ""
        var extra = ""
        for (parameter: SearchParameter in params) {
            when (StreamPlayQueryType.values()[parameter.queryType]){
                StreamPlayQueryType.PARAMETER -> {
                    if (parameters.isNotEmpty()) {
                        parameters += "%20AND%20"
                    }
                    parameters += parameterString(parameter)
                }
                StreamPlayQueryType.MEDIA -> {
                    if (media.isNotEmpty()) {
                        media += "%20AND%20"
                    }
                    media += parameterString(parameter)
                }
                StreamPlayQueryType.EXTRA -> {
                    if (extra.isNotEmpty()) {
                        extra += "&"
                    }
                    extra += parameterString(parameter)
                }
            }

        }

        var param = "?"
        if (amend){
            param="&"
        }
        if (parameters.isNotEmpty()){
            param += "q=($parameters)"
        }
        if (media.isNotEmpty()){
            if (param.length>1){
                param += "&"
            }
            param += "q=($media)"
        }
        if (extra.isNotEmpty()){
            if (param.length>1){
                param += "&"
            }
            param += extra
        }
        logSP(param)
        return param
    }

    private fun parameterString(parameter: SearchParameter): String {
return "${parameter.target}:${parameter.searchType.operator}${parameter.query}"
    }

    private fun baseURL(): String {
        return when (StreamAMGSDK.getInstance().environment) {
            StreamAPIEnvironment.PRODUCTION -> {
                "https://api.streamplay.streamamg.com/"
            }

            else -> {
                "https://staging.api.streamplay.streamamg.com/"
            }
        }
    }

    private fun pagination(guaranteedURL: String): String {
        var pagination = guaranteedURL
        if (!pagination.contains("?")){
            pagination += "?"
        } else {
            pagination += "&"
        }
        pagination += "offset=$currentOffset"
        var paginateBy = paginateBy
        if (paginateBy > 0){
            pagination += "&limit=$paginateBy"
        }
        return pagination
    }

    /**
     * Builder class for creating 'FEED' queries
     * It is preferred that a builder class is used to ensure all relevant information is included
     */
    class FeedBuilder(
    ) {
        private var sport: ArrayList<StreamPlaySport> = ArrayList()
        private var fixtureID: String? = null
        private var partnerID: String? = null
        private var pagination: Int = 0
        private var url: String? = null

        /**
         * single 'StreamPlaySport' enum that should form the basis of the feed - should not be used with 'url'
         */
        fun sport(sport: StreamPlaySport) = apply {
            this.sport.add(sport)
        }

        /**
         * Array list of 'StreamPlaySport' enums that should form the basis of the feed - should not be used with 'url'
         */
        fun sports(sports: ArrayList<StreamPlaySport>) = apply {
            this.sport.addAll(sports)
        }

        /**
         * ID of the fixture being requested - should not be used with 'url'
         */
        fun fixture(fixture: String) = apply {
            this.fixtureID = fixture
        }

        /**
         * ID of the 'partner' making the request - should not be used with 'url'
         */
        fun partner(partner: String) = apply {
            this.partnerID = partner
        }

        /**
         * Adds a fully formed URL to the query - should not be used with other request items
         */
        fun url(url: String) = apply {
            this.url = url
        }

        /**
         * A requested number of items per page to be returned - default is 20
         */
        fun paginateBy(paginateBy: Int) = apply {
            this.pagination = paginateBy
        }

        /**
         * Returns a StreamPlayRequest and logs to the console if this request is not valid
         */
        fun build(): StreamPlayRequest {
           if (sport.isEmpty() && url.isNullOrEmpty()){
               logErrorSP("A StreamPlay Sport must be provided in the Request Builder")
           }
                return StreamPlayRequest(sport = sport,fixtureID = fixtureID, url = url, paginateBy = pagination)

        }
    }

     class SearchBuilder(
    ) {
        var sport: ArrayList<StreamPlaySport> = ArrayList()
         var partnerID: String? = null
        var pagination: Int = 0
        var params: ArrayList<SearchParameter> = ArrayList()
         var workableURL: String? = null


         public fun url(url: String) = apply {
             this.workableURL = url
         }

         /**
          * single 'StreamPlaySport' enum that should form the basis of the feed - should not be used with 'url'
          */
         fun sport(sport: StreamPlaySport) = apply {
             this.sport.add(sport)
         }

         /**
          * Array list of 'StreamPlaySport' enums that should form the basis of the feed - should not be used with 'url'
          */
         fun sports(sports: ArrayList<StreamPlaySport>) = apply {
             this.sport.addAll(sports)
         }

         /**
          * ID of the 'partner' making the request - should not be used with 'url'
          */
         fun partner(partner: String) = apply {
             this.partnerID = partner
         }

         /**
          * Adds an 'equality' check to the query that returns only items in which this field contains a complete word or string that matches the query
          *
          * @param target A 'StreamPlayQueryField' enum indicating the field to query
          * @param query The actual data to query
          */
        fun isEqualTo(target: StreamPlayQueryField, query: String) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.EQUALS, target.query, query, queryType=target.queryType.ordinal))
        }

         /**
          * Adds a 'comparative' check to the query that returns only items in which this field contains a value (numerical or alphabetically) greater than the query
          *
          * @param target A 'StreamPlayQueryField' enum indicating the field to query
          * @param query The actual data to query
          */
        fun isGreaterThan(target: StreamPlayQueryField, query: String) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.GREATERTHAN, target.query, query, queryType=target.queryType.ordinal))
        }

         /**
          * Adds a 'comparative' check to the query that returns only items in which this field contains a value (numerical or alphabetically) greater than or equal to the query
          *
          * @param target A 'StreamPlayQueryField' enum indicating the field to query
          * @param query The actual data to query
          */
        fun isGreaterThanOrEqualTo(target: StreamPlayQueryField, query: String) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.GREATERTHANOREQUALTO, target.query, query, queryType=target.queryType.ordinal))
        }

         /**
          * Adds a 'comparative' check to the query that returns only items in which this field contains a value (numerical or alphabetically) less than the query
          *
          * @param target A 'StreamPlayQueryField' enum indicating the field to query
          * @param query The actual data to query
          */
        fun isLessThan(target: StreamPlayQueryField, query: String) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.LESSTHAN, target.query, query, queryType=target.queryType.ordinal))
        }

         /**
          * Adds a 'comparative' check to the query that returns only items in which this field contains a value (numerical or alphabetically) less than or equal to the query
          *
          * @param target A 'StreamPlayQueryField' enum indicating the field to query
          * @param query The actual data to query
          */
        fun isLessThanOrEqualTo(target: StreamPlayQueryField, query: String) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.LESSTHANOREQUALTO, target.query, query, queryType=target.queryType.ordinal))
        }

         /**
          * Adds an 'equality' check to the query that returns only items in which this field contains a number equal to the query
          *
          * @param target A 'StreamPlayQueryField' enum indicating the field to query
          * @param query The actual data to query
          */
        fun isEqualTo(target: StreamPlayQueryField, query: Number) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.EQUALS, target.query, query.toString(), queryType=target.queryType.ordinal))
        }

         /**
          * Adds a 'comparative' check to the query that returns only items in which this field contains a numerical value less than the query
          *
          * @param target A 'StreamPlayQueryField' enum indicating the field to query
          * @param query The actual data to query
          */
        fun isGreaterThan(target: StreamPlayQueryField, query: Number) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.GREATERTHAN, target.query, query.toString(), queryType=target.queryType.ordinal))
        }

         /**
          * Adds a 'comparative' check to the query that returns only items in which this field contains a numerical value greater than or equal to the query
          *
          * @param target A 'StreamPlayQueryField' enum indicating the field to query
          * @param query The actual data to query
          */
        fun isGreaterThanOrEqualTo(target: StreamPlayQueryField, query: Number) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.GREATERTHANOREQUALTO, target.query, query.toString(), queryType=target.queryType.ordinal))
        }

         /**
          * Adds a 'comparative' check to the query that returns only items in which this field contains a numerical value less than the query
          *
          * @param target A 'StreamPlayQueryField' enum indicating the field to query
          * @param query The actual data to query
          */
        fun isLessThan(target: StreamPlayQueryField, query: Number) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.LESSTHAN, target.query, query.toString(), queryType=target.queryType.ordinal))
        }

         /**
          * Adds a 'comparative' check to the query that returns only items in which this field contains a numerical value less than or equal to the query
          *
          * @param target A 'StreamPlayQueryField' enum indicating the field to query
          * @param query The actual data to query
          */
        fun isLessThanOrEqualTo(target: StreamPlayQueryField, query: Number) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.LESSTHANOREQUALTO, target.query, query.toString(), queryType=target.queryType.ordinal))
        }

         /**
          * Adds a 'fuzzy' check to the query that returns only items in which this field where any word or continual string of the value contains the query
          *
          * @param target A 'StreamPlayQueryField' enum indicating the field to query
          * @param query The actual data to query
          */
        fun isLike(target: StreamPlayQueryField, query: String) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.FUZZY, target.query, "*$query*", queryType=target.queryType.ordinal))
        }

         /**
          * Adds a 'fuzzy' check to the query that returns only items in which this field where any word or continual string of the value starts with the query
          *
          * @param target A 'StreamPlayQueryField' enum indicating the field to query
          * @param query The actual data to query
          */
        fun startsWith(target: StreamPlayQueryField, query: String) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.EQUALS, target.query, "$query*", queryType=target.queryType.ordinal))
        }

         /**
          * Adds a 'fuzzy' check to the query that returns only items in which this field where any word or continual string of the value ends with the query
          *
          * @param target A 'StreamPlayQueryField' enum indicating the field to query
          * @param query The actual data to query
          */
         fun endsWith(target: StreamPlayQueryField, query: String) = apply {
             this.params.add(SearchParameter(StreamAMGQueryType.EQUALS, target.query, "*$query", queryType=target.queryType.ordinal))
         }

         /**
          * Adds a boolean check to the query that returns only items in this field which are 'true'
          *
          * @param target A 'StreamPlayQueryField' enum indicating the field to query
          */
        fun isTrue(target: StreamPlayQueryField) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.EQUALS, target.query, "true", queryType=target.queryType.ordinal))
        }

         /**
          * Adds a boolean check to the query that returns only items in this field which are 'false'
          *
          * @param target A 'StreamPlayQueryField' enum indicating the field to query
          */
        fun isFalse(target: StreamPlayQueryField) = apply {
            this.params.add(SearchParameter(StreamAMGQueryType.EQUALS, target.query, "false", queryType=target.queryType.ordinal))
        }

         /**
          * Indicates which field should be sorted by in ascending value
          *
          * @param target A 'StreamPlayQueryField' enum indicating the field to query
          */
         fun sortByAscending(target: StreamPlayQueryField) = apply {
             this.params.add(SearchParameter(StreamAMGQueryType.EQUALS, target.query, "sort=${target.query}:asc", queryType=StreamPlayQueryType.EXTRA.ordinal))
         }

         /**
          * Indicates which field should be sorted by in descending value
          *
          * @param target A 'StreamPlayQueryField' enum indicating the field to query
          */
         fun sortByDescending(target: StreamPlayQueryField) = apply {
             this.params.add(SearchParameter(StreamAMGQueryType.EQUALS, target.query, "sort=${target.query}:desc", queryType=StreamPlayQueryType.EXTRA.ordinal))
         }

         /**
          * Indicates that any date queries should use the 'End' date of the item
          */
         fun endDateEffective() = apply {
             this.params.add(SearchParameter(StreamAMGQueryType.EQUALS, "", "dateField=endDate", queryType=StreamPlayQueryType.EXTRA.ordinal))
         }

         /**
          * Indicates that any date queries should use the 'Start' date of the item
          */
         fun startDateEffective() = apply {
             this.params.add(SearchParameter(StreamAMGQueryType.EQUALS, "", "dateField=startDate", queryType=StreamPlayQueryType.EXTRA.ordinal))
         }

         /**
          * Adds a date check to the query that returns only items where the 'effective' date is after the query
          */
         fun dateFrom(date: String) = apply {
             this.params.add(SearchParameter(StreamAMGQueryType.EQUALS, "", "from=$date", queryType=StreamPlayQueryType.EXTRA.ordinal))
         }

         /**
          * Adds a date check to the query that returns only items where the 'effective' date is before the query
          */
         fun dateTo(date: String) = apply {
             this.params.add(SearchParameter(StreamAMGQueryType.EQUALS, "", "to=$date", queryType=StreamPlayQueryType.EXTRA.ordinal))
         }

         /**
          * A requested number of items per page to be returned - default is 20
          */
        fun paginateBy(paginateBy: Int) = apply {
            this.pagination = paginateBy
        }

         /**
          * Returns a StreamPlayRequest and logs to the console if this request is not valid
          */
         fun build(): StreamPlayRequest {
             if (sport.isEmpty() && workableURL == null) {
                 logErrorSP("A StreamPlay Sport must be provided in the Request Builder")
             }
             return StreamPlayRequest(sport = sport, partnerID = partnerID, params = params, url = workableURL, paginateBy = pagination)
         }
    }

}