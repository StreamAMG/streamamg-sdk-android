package com.streamamg.streamapi_core

import com.streamamg.streamapi_core.constants.StreamAMGQueryType
import junit.framework.Assert.assertTrue
import org.junit.Test

class StreamAMGCoreValueTests: BaseTestClass() {
    @Test
    fun testQueryTypeValues() {
        assertTrue(StreamAMGQueryType.GREATERTHAN.description == "is greater than")
        assertTrue(StreamAMGQueryType.GREATERTHANOREQUALTO.description == "is greater than or equal to")
        assertTrue(StreamAMGQueryType.LESSTHAN.description == "is less than")
        assertTrue(StreamAMGQueryType.LESSTHANOREQUALTO.description == "is less than or equal to")
        assertTrue(StreamAMGQueryType.EQUALS.description == "is equal to")
        assertTrue(StreamAMGQueryType.EXISTS.description == "exists")
        assertTrue(StreamAMGQueryType.FUZZY.description == "is like")
        assertTrue(StreamAMGQueryType.WILDCARD.description == "starts with")

        assertTrue(StreamAMGQueryType.GREATERTHAN.operator == ">")
        assertTrue(StreamAMGQueryType.GREATERTHANOREQUALTO.operator == ">=")
        assertTrue(StreamAMGQueryType.LESSTHAN.operator == "<")
        assertTrue(StreamAMGQueryType.LESSTHANOREQUALTO.operator == "<=")
        assertTrue(StreamAMGQueryType.EQUALS.operator == "")
        assertTrue(StreamAMGQueryType.EXISTS.operator == "")
        assertTrue(StreamAMGQueryType.FUZZY.operator == "")
        assertTrue(StreamAMGQueryType.WILDCARD.operator == "")
    }
}