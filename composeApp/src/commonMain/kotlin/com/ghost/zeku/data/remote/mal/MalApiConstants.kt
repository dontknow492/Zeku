package com.ghost.zeku.data.remote.mal

object MalApiConstants {

    const val COMMON_FIELDS = """
        id,title,main_picture,alternative_titles,
        synopsis,mean,status,genres,start_date,
        my_list_status
    """

    const val EXTRA_FIELDS = """
        end_date,rank,popularity,
        num_list_users,num_scoring_users,
        nsfw,created_at,updated_at,
        media_type,pictures,background,
        related_anime,related_manga,
        recommendations,statistics
    """

    const val ANIME_FIELDS = """
        $COMMON_FIELDS,
        num_episodes,
        average_episode_duration
    """

    const val MANGA_FIELDS = """
        $COMMON_FIELDS,
        num_chapters,
        num_volumes
    """

    const val ANIME_DETAILS_FIELDS = """
        $ANIME_FIELDS,
        $EXTRA_FIELDS,
        start_season,broadcast,
        source,rating,studios
    """

    const val MANGA_DETAILS_FIELDS = """
        $MANGA_FIELDS,
        $EXTRA_FIELDS,
        authors,serialization
    """
}