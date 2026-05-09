package com.ghost.zeku.data.local.room

import androidx.room.RoomRawQuery
import com.ghost.zeku.domain.model.filter.MediaFilterState
import com.ghost.zeku.domain.model.filter.SortOption


object MediaQueryFactory {

    fun buildQuery(state: MediaFilterState): RoomRawQuery {
        val conditions = mutableListOf<String>()
        val args = mutableListOf<Any>()

        // Use ONE StringBuilder for the entire query
        val sql = StringBuilder("SELECT * FROM MediaLibraryView ")

        // 1. Handle Joins
        // FTS Search Join (Only if searching)
        if (state.searchQuery.isNotBlank()) {
            sql.append(
                """
                JOIN media_search ON media.id = media_search.mediaId 
                AND media.provider = media_search.provider 
            """.trimIndent()
            )

            conditions.add("media_search MATCH ?")
            args.add("${state.searchQuery}*")
        }

        // Library Join (Always needed for sorting/filtering by library stats)
        sql.append(
            """
            LEFT JOIN library ON media.id = library.mediaId 
            AND media.provider = library.provider 
        """.trimIndent()
        )

        // 2. Genre Filtering (Include/Exclude)
        state.includedGenres.forEach { genre ->
            conditions.add("media.genres LIKE ?")
            args.add("%$genre%")
        }
        state.excludedGenres.forEach { genre ->
            conditions.add("media.genres NOT LIKE ?")
            args.add("%$genre%")
        }

        // 3. Media Type & Status
        if (state.mediaTypes.isNotEmpty()) {
            val placeholders = state.mediaTypes.joinToString(",") { "?" }
            conditions.add("media.mediaType IN ($placeholders)")
            args.addAll(state.mediaTypes.map { it.name })
        }

        // 4. Year Range
        state.yearRange?.let {
            conditions.add("media.seasonYear BETWEEN ? AND ?")
            args.add(it.first)
            args.add(it.last)
        }

        // 5. Combine Conditions into WHERE clause
        if (conditions.isNotEmpty()) {
            sql.append(" WHERE ${conditions.joinToString(" AND ")}")
        }

        // 6. Sorting
        val orderBy = when (state.sortBy) {
            SortOption.ADDED_AT -> "library.addedAt"
            SortOption.SYNCED_AT -> "library.syncedAt"
            else -> "media.${state.sortBy.sqlColumnName}"
        }
        sql.append(" ORDER BY $orderBy ${state.sortDirection.sqlKeyword}")

        // 7. Create the RoomRawQuery using the combined string
        return RoomRawQuery(sql.toString()) { statement ->
            args.forEachIndexed { index, arg ->
                val bindIndex = index + 1 // SQLite is 1-indexed
                when (arg) {
                    is String -> statement.bindText(bindIndex, arg)
                    is Int -> statement.bindLong(bindIndex, arg.toLong())
                    is Long -> statement.bindLong(bindIndex, arg)
                    is Float -> statement.bindDouble(bindIndex, arg.toDouble())
                    is Double -> statement.bindDouble(bindIndex, arg)
                    is Boolean -> statement.bindLong(bindIndex, if (arg) 1L else 0L)
                }
            }
        }
    }
}




