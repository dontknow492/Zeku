package com.ghost.zeku

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform