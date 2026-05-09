package com.ghost.zeku.data.local.room

import androidx.room.RoomRawQuery
import com.ghost.zeku.domain.model.filter.MediaFilterState
import com.ghost.zeku.domain.model.filter.SortOption


object MediaQueryFactory {

    fun buildQuery(state: MediaFilterState, requireLibrary: Boolean = true): RoomRawQuery {
        val conditions = mutableListOf<String>()
        val args = mutableListOf<Any>()

        // 1. Base Query using the View
        val sql = StringBuilder("SELECT * FROM media_library_view ")

        // 2. FTS Search Join
        // We join directly using 'id' and 'provider' because they exist at the root of the view
        if (state.searchQuery.isNotBlank()) {
            sql.append(
                """
                JOIN media_search ON id = media_search.mediaId 
                AND provider = media_search.provider 
                """.trimIndent()
            )

            conditions.add("media_search MATCH ?")
            args.add("${state.searchQuery}*")
        }

        // 2. Enforce Library Ownership
        // CRITICAL FIX: If we only want library items, lib_id MUST NOT BE NULL
        if (requireLibrary) {
            conditions.add("lib_id IS NOT NULL")
        }

        // 3. Genre Filtering (Include/Exclude)
        // Removed the "media." prefix. The view just outputs "genres"
        state.includedGenres.forEach { genre ->
            conditions.add("genres LIKE ?")
            args.add("%$genre%")
        }
        state.excludedGenres.forEach { genre ->
            conditions.add("genres NOT LIKE ?")
            args.add("%$genre%")
        }

        // 4. Media Type & Status
        if (state.mediaTypes.isNotEmpty()) {
            val placeholders = state.mediaTypes.joinToString(",") { "?" }
            conditions.add("mediaType IN ($placeholders)")
            args.addAll(state.mediaTypes.map { it.name })
        }

        // 5. Year Range
        state.yearRange?.let {
            conditions.add("seasonYear BETWEEN ? AND ?")
            args.add(it.first)
            args.add(it.last)
        }

        // 6. Combine Conditions into WHERE clause
        if (conditions.isNotEmpty()) {
            sql.append(" WHERE ${conditions.joinToString(" AND ")}")
        }

        // 7. Sorting
        // Map SortOption to the exact aliases we defined inside the DatabaseView
        val orderBy = when (state.sortBy) {
            SortOption.ADDED_AT -> "lib_addedAt"
            SortOption.SYNCED_AT -> "lib_syncedAt"
            else -> state.sortBy.sqlColumnName // Removed "media." prefix
        }

        sql.append(" ORDER BY $orderBy ${state.sortDirection.sqlKeyword}")

        // 8. Create the RoomRawQuery
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




