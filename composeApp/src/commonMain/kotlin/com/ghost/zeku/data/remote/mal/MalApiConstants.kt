package com.ghost.zeku.data.remote.mal

object MalApiConstants {
    // Centralized fields to easily add or remove data we want to fetch
    const val ANIME_FIELDS =
        "id,title,main_picture,alternative_titles,synopsis,num_episodes,average_episode_duration,mean,status,genres,start_date,my_list_status"
    const val MANGA_FIELDS =
        "id,title,main_picture,alternative_titles,synopsis,num_chapters,num_volumes,mean,status,genres,start_date,my_list_status"

    const val ANIME_DETAILS_FIELDS =
        "id,title,main_picture,alternative_titles,start_date,end_date,synopsis,mean,rank,popularity,num_list_users,num_scoring_users,nsfw,created_at,updated_at,media_type,status,genres,my_list_status,num_episodes,start_season,broadcast,source,average_episode_duration,rating,pictures,background,related_anime,related_manga,recommendations,studios,statistics"

    const val MANGA_DETAILS_FIELDS =
        "id,title,main_picture,alternative_titles,start_date,end_date,synopsis,mean,rank,popularity,num_list_users,num_scoring_users,nsfw,created_at,updated_at,media_type,status,genres,my_list_status,num_chapters,num_volumes,pictures,background,related_anime,related_manga,recommendations,authors"

}