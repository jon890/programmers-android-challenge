package com.programmers.flomusicplayer.utils

import com.programmers.flomusicplayer.models.LyricsItem
import com.programmers.flomusicplayer.models.StartTime

object StringUtil {

    fun parseLyrics(lyricsString: String): List<LyricsItem> {
        val list = mutableListOf<LyricsItem>()
        val splits = lyricsString.split("\n")

        for (str in splits) {
            val boundary = str.indexOf("]") + 1
            val startTimeArray = str.substring(0, boundary)
                .replace("[", "")
                .replace("]", "")
                .split(":")
            val startTime = StartTime(
                Integer.parseInt(startTimeArray[0]),
                Integer.parseInt(startTimeArray[1]),
                Integer.parseInt(startTimeArray[2])
            )
            val lyrics = str.substring(boundary).trim()
            list.add(LyricsItem(startTime, lyrics))
        }

        return list
    }

    /**
     * 노래의 Duration을 00:00으로 표시
     * duration은 ms단위로 넣을 것
     */
    fun formatSongDuration(duration : Int) : String {
        val totalSeconds = duration / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60
        return "${String.format("%02d", minutes)}:${String.format("%02d", seconds)}"
    }
}