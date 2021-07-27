package com.streamamg.streamapi_core.constants

enum class StreamAMGQueryType(val operator: String, val description: String) {
   // NONE("", ""),
    EQUALS("", "is equal to"),
    GREATERTHAN(">", "is greater than"),
    GREATERTHANOREQUALTO(">=", "is greater than or equal to"),
    LESSTHAN("<", "is less than"),
    LESSTHANOREQUALTO("<=", "is less than or equal to"),
    EXISTS("", "exists"),
    FUZZY("", "is like"),
    WILDCARD("", "starts with")
}