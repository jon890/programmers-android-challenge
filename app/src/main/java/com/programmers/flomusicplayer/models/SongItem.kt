package com.programmers.flomusicplayer.models

data class SongItem(
    val singer: String, // 가수 이름
    var album: String, // 앨범 이름
    var title: String, // 노래 제목
    val duration: Int, // 노래 길이
    val imageUrl: String, // 이미지 Url
    val fileUrl: String, // 노래 Url
    val lyricsList: List<LyricsItem>, // 가사 목록
)