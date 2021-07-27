package com.streamamg.streamapi_streamplay.constants

enum class StreamPlayQueryField(val query: String, val queryDescription: String, val queryType: StreamPlayQueryType = StreamPlayQueryType.PARAMETER) {
    ID("id", "Item ID"),
    MEDIA_TYPE("mediaData.mediaType", "Media Type", StreamPlayQueryType.MEDIA),
    MEDIA_ENTRYID("mediaData.entryId", "Entry ID", StreamPlayQueryType.MEDIA),
    MEDIA_DRM("mediaData.drm", "Entry Status", StreamPlayQueryType.MEDIA),
    FIXTURE_TYPE("type", "Fixture type"),
    FIXTURE_NAME("name", "Fixture name"),
    FIXTURE_DESCRIPTION("description", "Fixture description"),
    FIXTURE_OPTA_ID("externalIds.optaFixtureId", "Opta ID"),
    FIXTURE_SPORTS_RADAR_ID("externalIds.sportsradarFixtureId", "Sports Radar ID"),
    FIXTURE_PA_ID("externalIds.paFixtureId", "PA ID"),
    SEASON_ID("season.id", "Season ID"),
    SEASON_NAME("season.name", "Season name"),
    COMPETITION_ID("competition.id", "Competition ID"),
    COMPETITION_NAME("competition.name", "Competition name"),
    HOME_TEAM_ID("homeTeam.id", "Home Team ID"),
    HOME_TEAM_NAME("homeTeam.name", "Home Team name"),
    AWAY_TEAM_ID("awayTeam.id", "Away Team ID"),
    AWAY_TEAM_NAME("awayTeam.name", "Away Team name"),
    STADIUM_ID("stadium.id", "Stadium ID"),
    STADIUM_NAME("stadium.name", "Stadium name"),
    LOCATION_ID("location.id", "Location ID"),
    LOCATION_NAME("location.name", "Location name"),
    EVENT_TYPE("eventType", "Event type")
}

enum class StreamPlayQueryType {
    PARAMETER,
    MEDIA,
    EXTRA
}