package com.swordfish.lemuroid.lib.storage.local

/** Names used by the local media layout for one ROM base name. */
internal fun artworkDirectoryNames(baseName: String): List<String> {
    val names = mutableListOf(baseName)
    listOf(" 原版", " 完善版", " 正篇", " 特别篇")
        .firstOrNull(baseName::endsWith)
        ?.let { names += baseName.removeSuffix(it) }

    if (baseName.startsWith("白夜BGM重置版")) {
        val version = baseName.removePrefix("白夜BGM重置版").takeWhile(Char::isDigit)
        if (version.isNotEmpty()) {
            names += "恶魔城2 白夜协奏曲BGM替换版$version"
        }
    } else if (baseName.startsWith("白夜终极版")) {
        names += "恶魔城2 白夜协奏曲改版"
    }
    return names
}
