package com.ghost.zeku.data.remote.anilist

/**
 * A centralized, fragment-based GraphQL query builder for AniList.
 * Designed for easy updates and minimal code duplication.
 */
object AniListQueries {

    // =============================================================================================
    // 1. FRAGMENTS (The "Lego Bricks")
    // =============================================================================================

    private const val PAGE_INFO = """
        pageInfo {
            total
            currentPage
            lastPage
            hasNextPage
        }
    """

    private const val MEDIA_CORE_FIELDS = """
        id
        type
        title { romaji english native userPreferred }
        synonyms
        countryOfOrigin
        coverImage { large extraLarge }
        bannerImage
        description(asHtml: false)
        status
        format
        source
        isAdult
        startDate { year month day }
        endDate { year month day }
        season
        seasonYear
        genres
        tags { name description rank isMediaSpoiler category }
        averageScore
        meanScore
        popularity
        favourites
    """

    private const val MEDIA_LIST_ENTRY_FIELDS = """
        id
        mediaId
        status
        progress
        score
    """

    // Specific fields based on type
    private fun getExtraFields(type: String) = when (type.uppercase()) {
        "ANIME" -> """
            episodes 
            duration 
            nextAiringEpisode { episode timeUntilAiring }
            studios { edges { isMain node { id name isAnimationStudio } } }
            staff { edges { role node { id name { userPreferred } image { large } } } }
        """.trimIndent()

        "MANGA" -> """
            chapters 
            volumes
            staff { edges { role node { id name { userPreferred } image { large } } } }
        """.trimIndent()

        else -> ""
    }

    // =============================================================================================
    // 2. PUBLIC QUERIES (The "Final Products")
    // =============================================================================================

    // Discovery / Global Search
    // 1. Discovery (Trending/Top Rated)
    val FETCH_ANIME_LIST = buildMediaQuery("ANIME", isSearch = true)
    val FETCH_MANGA_LIST = buildMediaQuery("MANGA", isSearch = true)

    val FETCH_ANIME_HERO_LIST = buildMediaQuery("ANIME", isSearch = true)
    val FETCH_MANGA_HERO_LIST = buildMediaQuery("MANGA", isSearch = true)

    // 2. Global Search (Requires the 'search' variable)
    val SEARCH_ANIME = buildMediaQuery("ANIME", isSearch = true)
    val SEARCH_MANGA = buildMediaQuery("MANGA", isSearch = true)

    // Details
    val GET_ANIME_DETAILS = buildDetailsQuery("ANIME")
    val GET_MANGA_DETAILS = buildDetailsQuery("MANGA")

    // User Profile & Social
    val GET_USER_VIEWER = """
        query {
            Viewer {
                id
                name
                avatar { large medium }
                statistics {
                    anime { count episodesWatched minutesWatched }
                    manga { count chaptersRead }
                }
            }
        }
    """.trimIndent()

    // User Lists
    val GET_USER_ANIME_LIST = buildUserListQuery("ANIME")
    val GET_USER_MANGA_LIST = buildUserListQuery("MANGA")

    // Generic Search in User List
    val SEARCH_USER_LIST = buildUserListQuery(type = "${'$'}type", isGeneric = true)

    // Mutations
    val UPDATE_ENTRY = """
        mutation(${'$'}mediaId: Int, ${'$'}progress: Int, ${'$'}status: MediaListStatus, ${'$'}score: Float) {
            SaveMediaListEntry(mediaId: ${'$'}mediaId, progress: ${'$'}progress, status: ${'$'}status, scoreRaw: ${'$'}score) {
                $MEDIA_LIST_ENTRY_FIELDS
            }
        }
    """.trimIndent()

    val DELETE_ENTRY = """
        mutation(${'$'}id: Int) {
            DeleteMediaListEntry(id: ${'$'}id) { deleted }
        }
    """.trimIndent()


    // ========================================================================
    // LAZY DETAILS (Paginated API calls)
    // ========================================================================

    const val GET_MEDIA_RECOMMENDATIONS = """
        query(${'$'}id: Int, ${'$'}page: Int) {
            Media(id: ${'$'}id) {
                recommendations(page: ${'$'}page, perPage: 20, sort: [RATING_DESC]) {
                    pageInfo { total perPage currentPage lastPage hasNextPage }
                    nodes {
                        mediaRecommendation {
                            $MEDIA_CORE_FIELDS
                            format
                            episodes chapters duration
                        }
                    }
                }
            }
        }
    """

    const val GET_MEDIA_REVIEWS = """
        query(${'$'}id: Int, ${'$'}page: Int) {
            Media(id: ${'$'}id) {
                reviews(page: ${'$'}page, perPage: 20, sort: [RATING_DESC]) {
                    pageInfo { total perPage currentPage lastPage hasNextPage }
                    nodes {
                        id summary body score rating ratingAmount
                        user { name avatar { large } }
                        createdAt
                    }
                }
            }
        }
        """

    // =============================================================================================
    // 3. INTERNAL BUILDERS (The "Factories")
    // =============================================================================================

    /**
     * Builds a query for the global Media Page (Search or Trending).
     */
    /**
     * Builds a query for the global Media Page.
     * @param type "ANIME" or "MANGA"
     * @param isSearch If true, injects the advanced filter arguments into the GraphQL query.
     */
    private fun buildMediaQuery(type: String, isSearch: Boolean = false): String {

        // 1. Add the variable declarations to the top of the GraphQL query
        val searchSignature = if (isSearch) {
            ", ${'$'}search: String, ${'$'}seasonYear: Int, ${'$'}season: MediaSeason, ${'$'}format: MediaFormat, ${'$'}status: MediaStatus, ${'$'}genre_in: [String], ${'$'}genre_not_in: [String], ${'$'}tag_in: [String], ${'$'}tag_not_in: [String], ${'$'}isAdult: Boolean, ${'$'}countryOfOrigin: CountryCode"
        } else ""

        // 2. Pass the variables into the actual media() request
        val searchArgs = if (isSearch) {
            ", search: ${'$'}search, seasonYear: ${'$'}seasonYear, season: ${'$'}season, format: ${'$'}format, status: ${'$'}status, genre_in: ${'$'}genre_in, genre_not_in: ${'$'}genre_not_in, tag_in: ${'$'}tag_in, tag_not_in: ${'$'}tag_not_in, isAdult: ${'$'}isAdult, countryOfOrigin: ${'$'}countryOfOrigin"
        } else ""

        return """
            query(${'$'}page: Int, ${'$'}perPage: Int, ${'$'}sort: [MediaSort]$searchSignature) {
                Page(page: ${'$'}page, perPage: ${'$'}perPage) {
                    $PAGE_INFO
                    media(type: $type, sort: ${'$'}sort$searchArgs) {
                        $MEDIA_CORE_FIELDS
                        ${getExtraFields(type)}
                    }
                }
            }
        """.trimIndent()
    }


    /**
     * Builds a detailed query for a single Media item.
     */
    private fun buildDetailsQuery(type: String): String {
        return """
            query(${'$'}id: Int) {
                Media(id: ${'$'}id, type: $type) {
                    $MEDIA_CORE_FIELDS
                    trailer { id site thumbnail }
                    externalLinks { url site icon }
                    characters(sort: [ROLE, RELEVANCE], perPage: 15) {
                        edges {
                            role
                            node { id name { userPreferred } image { large } }
                        }
                    }
                    relations {
                        edges {
                            relationType
                            node { 
                                id type format status averageScore episodes chapters
                                title { romaji english native userPreferred } 
                                coverImage { large } 
                            }
                        }
                    }
                    mediaListEntry { id status score progress }
                    ${getExtraFields(type)}
                }
            }
        """.trimIndent()
    }

    /**
     * Builds a query for a User's personal collection.
     */
    private fun buildUserListQuery(type: String, isGeneric: Boolean = false): String {
        val typeParam = if (isGeneric) "${'$'}type" else type
        val searchQuery = if (isGeneric) ", search: ${'$'}search" else ""
        val searchVar = if (isGeneric) ", ${'$'}search: String, ${'$'}type: MediaType" else ""

        return """
            query(${'$'}userId: Int, ${'$'}status: MediaListStatus, ${'$'}page: Int, ${'$'}perPage: Int $searchVar) {
                Page(page: ${'$'}page, perPage: ${'$'}perPage) {
                    $PAGE_INFO
                    mediaList(userId: ${'$'}userId, type: $typeParam, status: ${'$'}status $searchQuery, sort: UPDATED_TIME_DESC) {
                        $MEDIA_LIST_ENTRY_FIELDS
                        media {
                            $MEDIA_CORE_FIELDS
                            ${
            if (isGeneric) "${getExtraFields("ANIME")} ${getExtraFields("MANGA")}" else getExtraFields(
                type
            )
        }
                        }
                    }
                }
            }
        """.trimIndent()
    }
}