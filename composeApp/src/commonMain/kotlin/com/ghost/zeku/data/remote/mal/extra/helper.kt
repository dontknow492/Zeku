package com.ghost.zeku.data.remote.mal.extra

fun getCurrentSeasonAndYear(): Pair<String, Int> {
    val calendar = java.util.Calendar.getInstance()
    val month = calendar.get(java.util.Calendar.MONTH) // 0-11
    val year = calendar.get(java.util.Calendar.YEAR)

    val season = when (month) {
        in 0..2 -> "WINTER"
        in 3..5 -> "SPRING"
        in 6..8 -> "SUMMER"
        else -> "FALL"
    }
    return season to year
}