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
        
        episodes 
        duration 
        nextAiringEpisode { episode timeUntilAiring }
        studios { edges { isMain node { id name isAnimationStudio } } }
        staff { edges { role node { id name { userPreferred } image { large } } } }
            
        chapters 
        volumes
        staff { edges { role node { id name { userPreferred } image { large } } } }
    """

    private const val MEDIA_DETAILS_FIELDS = """

    nextAiringEpisode {
        episode
        timeUntilAiring
    }

    trailer {
        id
        site
        thumbnail
    }

    studios {
        edges {
            isMain
            node {
                id
                name
                isAnimationStudio
            }
        }
    }

    staff {
        edges {
            role
            node {
                id
                name {
                    userPreferred
                }
                image {
                    large
                }
            }
        }
    }

    externalLinks {
        url
        site
        icon
    }

    characters(
        sort: [ROLE, RELEVANCE],
        perPage: 15
    ) {
        edges {
            role
            node {
                id
                name {
                    userPreferred
                }
                image {
                    large
                }
            }
        }
    }

    relations {
        edges {
            relationType
            node {
                id
                type
                format
                status
                averageScore
                episodes
                chapters

                title {
                    romaji
                    english
                    native
                    userPreferred
                }

                coverImage {
                    large
                }
            }
        }
    }

    mediaListEntry {
        id
        status
        score
        progress
    }
"""

    private const val MEDIA_LIST_ENTRY_FIELDS = """
        id
        mediaId
        status
        progress
        score
    """


    // =============================================================================================
    // 2. PUBLIC QUERIES (The "Final Products")
    // =============================================================================================

    // Discovery / Global Search
    // 1. Discovery (Trending/Top Rated)
    val FETCH_MEDIA_LIST = buildMediaQuery(isSearch = true)


    val FETCH_HERO_MEDIA = buildMediaQuery(isSearch = true)

    // 2. Global Search (Requires the 'search' variable)
    val SEARCH_MEDIA = buildMediaQuery(isSearch = true)

    // Details
    const val GET_MEDIA_DETAILS = """

    query(
        ${'$'}id: Int,
        ${'$'}type: MediaType
    ) {

        Media(
            id: ${'$'}id,
            type: ${'$'}type
        ) {

            $MEDIA_CORE_FIELDS

            $MEDIA_DETAILS_FIELDS
        }
    }
"""

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


    // Mutations
    val UPDATE_ENTRY = """
        mutation(${'$'}mediaId: Int, ${'$'}progress: Int, ${'$'}status: MediaListStatus, ${'$'}score: Float) {
            SaveMediaListEntry(mediaId: ${'$'}mediaId, progress: ${'$'}progress, status: ${'$'}status, scoreRaw: ${'$'}score) {
                id mediaId status progress score
            }
        }
    """.trimIndent()

    val DELETE_ENTRY = """
        mutation(${'$'}id: Int) {
            DeleteMediaListEntry(id: ${'$'}id) { deleted }
        }
    """.trimIndent()

    val FETCH_LIBRARY = """
        query(${'$'}userId: Int, ${'$'}type: MediaType) {
            MediaListCollection(userId: ${'$'}userId, type: ${'$'}type) {
                lists {
                    entries {
                        id mediaId status progress score
                        media {
                            id
                            title { userPreferred english romaji }
                            coverImage { large }
                            type
                        }
                    }
                }
            }
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
    private fun buildMediaQuery(
        isSearch: Boolean = false
    ): String {

        val searchSignature = if (isSearch) {
            """
        , ${'$'}search: String
        , ${'$'}seasonYear: Int
        , ${'$'}season: MediaSeason
        , ${'$'}format: MediaFormat
        , ${'$'}status: MediaStatus
        , ${'$'}genre_in: [String]
        , ${'$'}genre_not_in: [String]
        , ${'$'}tag_in: [String]
        , ${'$'}tag_not_in: [String]
        , ${'$'}isAdult: Boolean
        , ${'$'}countryOfOrigin: CountryCode
        """
        } else ""

        val searchArgs = if (isSearch) {
            """
        , search: ${'$'}search
        , seasonYear: ${'$'}seasonYear
        , season: ${'$'}season
        , format: ${'$'}format
        , status: ${'$'}status
        , genre_in: ${'$'}genre_in
        , genre_not_in: ${'$'}genre_not_in
        , tag_in: ${'$'}tag_in
        , tag_not_in: ${'$'}tag_not_in
        , isAdult: ${'$'}isAdult
        , countryOfOrigin: ${'$'}countryOfOrigin
        """
        } else ""

        return """
        query(
            ${'$'}page: Int,
            ${'$'}perPage: Int,
            ${'$'}type: MediaType,
            ${'$'}sort: [MediaSort]
            $searchSignature
        ) {
            Page(
                page: ${'$'}page,
                perPage: ${'$'}perPage
            ) {

                $PAGE_INFO

                media(
                    type: ${'$'}type,
                    sort: ${'$'}sort
                    $searchArgs
                ) {

                    $MEDIA_CORE_FIELDS
                }
            }
        }
    """.trimIndent()
    }


}