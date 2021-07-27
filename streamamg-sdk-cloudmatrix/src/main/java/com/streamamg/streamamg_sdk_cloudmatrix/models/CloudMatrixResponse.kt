package com.streamamg.streamamg_sdk_cloudmatrix.models

import com.streamamg.streamamg_sdk_cloudmatrix.services.logErrorCM
import com.streamamg.streamamg_sdk_cloudmatrix.services.logListCM
import com.streamamg.streamapi_core.models.BaseStreamResponse

/**
 * Model returned from a valid, successful call by a CloudMatrixRequest
 */
data class CloudMatrixResponse(
        val feedMetaData: CloudMatrixFeedMetaDataModel,
        val sections: ArrayList<CloudMatrixSectionModel>? = null,
        val itemData: ArrayList<CloudMatrixItemDataModel>? = null,
        var pagingData: CloudMatrixPagingDataModel? = null,
        var currentSection: Int = 0
) : BaseStreamResponse() {

   internal fun logResponse() {
        if (sections != null) {
            for (section: CloudMatrixSectionModel in sections) {
                logListCM( section.name ?: "")
                for (result: CloudMatrixItemDataModel in section.itemData
                        ?: ArrayList()) {
                    logListCM( result.getTitle() ?: "")
                }
            }
        } else {
            logSearchResults()
        }
    }

    /**
     * Logs a list of all returned titles to the console - debugging method only
     *
     * Core should be configured to have logging enabled
     */
    fun logSearchResults() {
        logErrorCM("CM --------------------------------------")
        if (itemData != null) {
            for (result: CloudMatrixItemDataModel in itemData) {
                logListCM(  result.getTitle() ?: "")
            }
        } else {
            logListCM(  "No results available")
        }
    }

    /**
     * Returns the total number of items available in the current request
     */
    override fun fetchTotal(): String {
        if (sections != null && currentSection < sections.size) {
            val section = sections[currentSection]
            return section.pagingData.totalCount.toString()
        }
        pagingData?.let {
            return it.totalCount.toString()
        }
        return "0"
    }

    /**
     * Returns the number of entries per page in this response
     */
    override fun fetchLimit(): String {
        if (sections != null) {
            val section = sections[currentSection]
            return section.pagingData.pageSize.toString()
        }
        pagingData?.let {
            return it.pageSize.toString()
        }
        return "0"
    }

    override fun fetchRetrieved(): String {
        if (sections != null) {
            val section = sections[currentSection]
            return section.pagingData.itemCount.toString()
        }
        pagingData?.let {
            return it.itemCount.toString()
        }
        return "0"
    }

    /**
     * Returns the current page this response contains - normalised to start at 1
     */
    override fun fetchPageNumber(): String {
        if (sections != null) {
            val section = sections[currentSection]
            return (section.pagingData.pageIndex + 1).toString()
        }
        pagingData?.let {
            return (it.pageIndex + 1).toString()
        }
        return "0"
    }

    /**
     * Returns the total number of pages available for the current request
     */
    override fun fetchPageTotal(): String {
        if (sections != null) {
            val section = sections[currentSection]
            return section.pagingData.pageCount.toString()
        }
        pagingData?.let {
            return it.pageCount.toString()
        }
        return "0"
    }

    /**
     * Returns the full paging data model for this response
     */
    fun fetchPagingData(section: Int? = null): CloudMatrixPagingDataModel {
        if (sections != null) {

            val section = sections[section ?: currentSection]
            return section.pagingData
        }
        pagingData?.let {
            return it
        }
        return CloudMatrixPagingDataModel()
    }

    /**
     * Returns the index of the next page available, or the current index if not
     */
    override fun nextPage(): Int {
        if (sections != null && currentSection < sections.size) {
            val section = sections[currentSection]
            var nextPage = section.pagingData.pageIndex
            nextPage+= 1
            if (nextPage >= section.pagingData.pageCount) {
                nextPage = section.currentPage
            }
            return nextPage    //section.currentPage * section.pagingData.pageSize
        } else {
            if (pagingData != null) {
                var nextPage = pagingData!!.pageIndex
                nextPage+= 1
                        if (nextPage >= pagingData!!.pageCount) {
                            nextPage = currentPage
                }
                return nextPage //* pagingData!!.pageSize
            }
        }
        return 0
    }

    /**
     * Returns the index of the previous page available, or 0 if not
     */
    override fun previousPage(): Int {
        if (sections != null && currentSection < sections.size) {
            val section = sections[currentSection]
            var previousPage = section.pagingData.pageIndex
            previousPage -= 1
            if (previousPage < 0) {
                previousPage =0
            }
            return previousPage //section.currentPage * section.pagingData.pageSize
        } else {
            if (pagingData != null) {
                var previousPage = pagingData!!.pageIndex
                previousPage -= 1
                if (previousPage < 0) {
                    previousPage = 0
                }
                return previousPage //currentPage * pagingData!!.pageSize
            }
        }
        return 0
    }

    /**
     * Returns the item data contained in this response
     */
    fun fetchResults(): java.util.ArrayList<CloudMatrixItemDataModel> {
        var results: ArrayList<CloudMatrixItemDataModel> = ArrayList()
        if (sections != null) {
            for (section: CloudMatrixSectionModel in sections) {
                    results.addAll(section.itemData
                            ?: ArrayList())
            }
            return results
        } else {
            if (itemData != null) {
                results.addAll(itemData)
        }
        }
        return results
    }

    /**
     * Returns the item data contained in a particular section of this response - returns an empty array if no data is available or the index is out of range
     *
     * @param section The index of the section required
     */
    fun fetchResults(section: Int): java.util.ArrayList<CloudMatrixItemDataModel> {
        var results: ArrayList<CloudMatrixItemDataModel> = ArrayList()
        if (sections != null) {
            if (section < sections.size) {
                results.addAll(sections[section].itemData
                    ?: ArrayList())
            }
            return results
        } else {
            if (itemData != null) {
                results.addAll(itemData)
            }
        }
        return results
    }
}

/**
 * Feed meta data model
 */
data class CloudMatrixFeedMetaDataModel(
        var id: String?,
        var name: String?,
        var title: String?,
        var description: String?,
        var target: String?
)

/**
 * Section model - Contains information regarding a block of identifiable data -
 */
data class CloudMatrixSectionModel(
        var id: String?,
        var name: String?,
        val itemData: ArrayList<CloudMatrixItemDataModel>? = ArrayList(),
        var pagingData: CloudMatrixPagingDataModel = CloudMatrixPagingDataModel(),
        var currentPage: Int = 0

)

/**
 * Item data model
 */
data class CloudMatrixItemDataModel(
        var id: String?,
        val mediaData: CloudMatrixMediaDataModel?,
        val metaData: CloudMatrixMetaDataModel?,  //JSONObject?,
        val sortData: ArrayList<CloudMatrixSortDataModel> = ArrayList(),
        val publicationData: CloudMatrixPublicationDataModel?
) {

    /**
     * Convenience method - Returns the MetaData 'Title' component
     */
    fun getTitle(): String? {
        metaData?.let {
            return it["title"] as String?
        }
        return null
    }
    /**
     * Convenience method - Returns the MetaData 'Body' component
     */
    fun getBody(): String? {
        metaData?.let {
            return it["body"] as String?
        }
        return null
    }
    /**
     * Convenience method - Returns the MetaData 'VideoDuration' component
     */
    fun getDuration(): Double? {
        metaData?.let {
            return it["VideoDuration"] as Double?
        }
        return null
    }
    /**
     * Convenience method - Returns the MetaData 'Tags' array
     */
    fun getTags(): ArrayList<String>? {
        metaData?.let {
            return it["tags"] as ArrayList<String>?
        }
        return null
    }
    /**
     * Convenience method - Calls the MetaData 'GetString' method
     */
    fun getMetaDataString(key: String): String? {
        metaData?.let {
            return it.getString(key)
        }
        return null
    }
    /**
     * Convenience method - Calls the MetaData 'GetLong' method
     */
    fun getMetaDataLong(key: String): Long? {
        metaData?.let {
            return it.getLong(key)
        }
        return null
    }
    /**
     * Convenience method - Calls the MetaData 'GetInt' method
     */
    fun getMetaDataInt(key: String): Int? {
        metaData?.let {
            return it.getInt(key)
        }
        return null
    }
    /**
     * Convenience method - Calls the MetaData 'GetDouble' method
     */
    fun getMetaDataDouble(key: String): Double? {
        metaData?.let {
            return it.getDouble(key)
        }
        return null
    }
    /**
     * Convenience method - Calls the MetaData 'GetBool' method
     */
    fun getMetaDataBool(key: String): Boolean? {
        metaData?.let {
            return it.getBool(key)
        }
        return null
    }
    /**
     * Convenience method - Calls the MetaData 'GetArray' method
     */
    fun getMetaDataArray(key: String): ArrayList<Any>? {
        metaData?.let {
            return it.getArray(key)
        }
        return null
    }
    /**
     * Convenience method - Calls the MetaData 'GetStringArray' method
     */
    fun getMetaDataStringArray(key: String): ArrayList<String>? {
        metaData?.let {
            return it.getStringArray(key)
        }
        return null
    }
}

/**
 * Paging data model - all data can be accessed directly from the root of the Response model
 */
data class CloudMatrixPagingDataModel(
        var totalCount: Int = 0,
        var itemCount: Int = 0,
        var pageCount: Int = 0,
        var pageSize: Int = 0,
        var pageIndex: Int = 0
)

/**
 * Media data model - contains information regarding any associated media
 */
data class CloudMatrixMediaDataModel(
        val mediaType: String?,
        val entryId: String?,
        val entryStatus: String?,
        val thumbnailUrl: String?
)

/**
 * Sort data model
 */
data class CloudMatrixSortDataModel(
        val feedId: String?,
        val sectionId: String?,
        val order: Int?
)

/**
 * Meta data hash map - contains a Key / Value list of the partner specific data contained in the MetaData section of a returned API JSON object
 */
class CloudMatrixMetaDataModel: HashMap<String, Any>(){

    /**
     * Guaranteed to exist, but MAY be null
     */
    var title: String? = null
        get() = getString("title")
    /**
     * Guaranteed to exist, but MAY be null
     */
    var body: String? = null
    get() = getString("body")
    /**
     * Guaranteed to exist, but MAY be null - returns the 'VideoDuration' property in seconds
     */
    var duration: Int? = null
        get() = getInt("VideoDuration")
    /**
     * Guaranteed to exist, but MAY be null
     */
    var category: String? = null
        get() = getString("category")
    /**
     * Guaranteed to exist, but MAY be null - A list of strings set as tags for this entry
     */
    var tags: ArrayList<String>? = null
        get() = getStringArray("tags")

    /**
     * Returns a specific String for a give Key of an item in the MetaData section of the returned API JSON object
     * Will return a null if the key does not exist or would return an object that is not a String value
     */
    fun getString(key: String): String? {
            return this[key] as String?
    }

    /**
     * Returns a specific 32 bit integer for a give Key of an item in the MetaData section of the returned API JSON object
     * Will return a null if the key does not exist, would return an object that is not an Int value or would return an Int that would overflow
     */
    fun getInt(key: String): Int? {
        return this[key] as Int?
    }

    /**
     * Returns a specific Double (or float as a Double) for a give Key of an item in the MetaData section of the returned API JSON object
     * Will return a null if the key does not exist or would return an object that is not a Double / Float value
     */
    fun getDouble(key: String): Double? {
        return this[key] as Double?
    }

    /**
     * Returns a specific 32 bit integer for a give Key of an item in the MetaData section of the returned API JSON object
     * Will return a null if the key does not exist or would return an object that is not a Long / Int value
     */
    fun getLong(key: String): Long? {
        return this[key] as Long?
    }

    /**
     * Returns a boolean value for a give Key of an item in the MetaData section of the returned API JSON object
     * Will return a null if the key does not exist or would return an object that is not a Boolean value
     */
    fun getBool(key: String): Boolean? {
        return this[key] as Boolean?
    }

    /**
     * Returns an undefined array for a give Key of an item in the MetaData section of the returned API JSON object
     * Will return a null if the key does not exist or is not an array
     * The array is an array of 'Any'. In most cases, StringArray should be used
     */
    fun getArray(key: String): ArrayList<Any>? {
        if (this[key] is ArrayList<*>) {
            return this[key] as ArrayList<Any>
        }
        return null
    }

    /**
     * Returns a guaranteed array of Strings for a give Key of an item in the MetaData section of the returned API JSON object
     * Will return a null if the key does not exist, is not an array or contains any elements that are not strings
     * The array is an array of 'Any'. In most cases, StringArray should be used
     */
    fun getStringArray(key: String): ArrayList<String>? {
        val value = this[key]?.let {valList ->
            if (valList is ArrayList<*>) {
                for (item: Any? in valList){
                    if (item == null || item !is String){
                        return null
                    }
                }
                return this[key] as ArrayList<String>
            }
        }
        return null
    }
}

/**
 * Publication data model - contains information regarding creation and editing dates, as well as publishing range
 */
data class CloudMatrixPublicationDataModel(
        val createdAt: String?,
        val updatedAt: String?,
        val released: Boolean?,
        val releaseFrom: String?,
        val releaseTo: String?
)