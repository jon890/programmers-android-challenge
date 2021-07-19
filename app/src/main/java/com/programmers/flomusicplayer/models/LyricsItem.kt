package com.programmers.flomusicplayer.models

data class LyricsItem(
    val startTime: StartTime, // 가사가 나타나야할 시간
    val lyrics: String, // 해당 가사
)

data class StartTime(
    val minute: Int,
    val second: Int,
    val milliseconds: Int
) {
    fun totalMillis(): Int {
        return (minute * 60 + second) * 1000 + milliseconds
    }
}