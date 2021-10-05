package com.streamamg.streamapi_core

import com.streamamg.streamapi_core.constants.StreamAMGQueryType
import com.streamamg.streamapi_core.models.SearchParameter
import com.streamamg.streamapi_core.models.StreamAMGError
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test

class StreamAMGCoreModelTests: BaseTestClass() {
    var searchParameters: ArrayList<SearchParameter> = ArrayList()
    fun createErrorModel(): StreamAMGError {
        return StreamAMGError(message = "Error Message")
    }

    @Test
    fun testErrorModel() {
        val model = createErrorModel()
        assertTrue(model.getErrorCode() == -1)
        assertTrue(model.getMessages() == "Error Message")
        assertTrue(model.getAllMessages() == arrayListOf("Error Message"))
    }

    @Test
    fun testMultiErrorModel() {
        val model = createErrorModel()
        model.addMessage("Another Error")
        assertTrue(model.getErrorCode() == -1)
        assertTrue(model.getMessages() == "Error Message | Another Error")
        assertTrue(model.getAllMessages() == arrayListOf("Error Message", "Another Error") )
    }

    @Test
    fun testMultiErrorModelPlus() {
        val model = createErrorModel()
        val extraErrors = arrayListOf("Another Error", "A Third Error", "The Last Error")
        extraErrors.forEach{message ->
                model.addMessage(message)
        }
        assertTrue(model.getErrorCode() == -1)
        extraErrors.forEach{message ->
                assertTrue(model.getMessages() == "Error Message | Another Error | A Third Error | The Last Error")
            assertTrue(model.getAllMessages().contains(message))
        }
    }

    @Test
    fun testErrorCodeModel() {
        var model = createErrorModel()
        model.setCode(404)
        assertTrue(model.getErrorCode() == 404)
        assertTrue(model.getMessages() == "Error Message")
        assertTrue(model.getAllMessages() ==  arrayListOf("Error Message") )
    }

    @Test
    fun testEmptyModel() {
        var model = StreamAMGError(message = "")
        assertTrue(model.getErrorCode() == -1)
        assertTrue(model.getMessages() == "No messages reported by API")
        assertTrue(model.getAllMessages() == arrayListOf(""))
    }

    // Search Parameters

    fun setUpSearchParameters() {
        searchParameters.clear()
        searchParameters.add(SearchParameter(StreamAMGQueryType.EQUALS, "1", "1"))
        searchParameters.add(SearchParameter(StreamAMGQueryType.EQUALS, "10000000", "10000000"))
        searchParameters.add(SearchParameter(StreamAMGQueryType.GREATERTHAN, "2", "1"))
        searchParameters.add(SearchParameter(StreamAMGQueryType.LESSTHAN, "2", "3"))
        searchParameters.add(SearchParameter(StreamAMGQueryType.GREATERTHANOREQUALTO, "2", "2"))
        searchParameters.add(SearchParameter(StreamAMGQueryType.LESSTHANOREQUALTO, "2", "2"))
        searchParameters.add(SearchParameter(StreamAMGQueryType.EXISTS, "TARGET", ""))
        searchParameters.add(SearchParameter(StreamAMGQueryType.FUZZY, "TARGET", "THE TARGET TEXT"))
        searchParameters.add(SearchParameter(StreamAMGQueryType.WILDCARD, "TARGET", "TARGET TEXT"))

    }

    fun runQuery(query: SearchParameter){

            when (query.searchType) {
                StreamAMGQueryType.EQUALS ->{
                assertTrue(query.searchType.operator == "")
                assertTrue(query.query == query.target)
            }
                StreamAMGQueryType.GREATERTHAN ->{
                assertTrue(query.searchType.operator == ">")
                assertTrue(query.target > query.query)
                }
                    StreamAMGQueryType.GREATERTHANOREQUALTO ->{
                assertTrue(query.searchType.operator == ">=")
                assertTrue(query.target >= query.query)
                    }
                        StreamAMGQueryType.LESSTHAN ->{
                assertTrue(query.searchType.operator == "<")
                assertTrue(query.target < query.query)
                        }
                        StreamAMGQueryType.LESSTHANOREQUALTO ->{
                assertTrue(query.searchType.operator == "<=")
                assertTrue(query.target <= query.query)
                        }


        StreamAMGQueryType.EXISTS ->{
                assertTrue(query.searchType.operator == "")
                assertFalse(query.target.isEmpty())
        }
        StreamAMGQueryType.FUZZY ->{
                assertTrue(query.searchType.operator == "")
                assertTrue(query.query.contains(query.target))
        }
        StreamAMGQueryType.WILDCARD ->{
                assertTrue(query.searchType.operator == "")
                assertTrue(query.query.startsWith(query.target, false))    //starts(with:query.target))
        }
            }
    }

    @Test
    fun testSimpleQueries() {
        searchParameters.forEach{query ->
                runQuery(query)
        }
    }

}