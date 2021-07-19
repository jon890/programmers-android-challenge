package com.programmers.flomusicplayer.network

import com.programmers.flomusicplayer.models.SongModel
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET

interface FloRestful {

    @GET("song.json")
    fun getSong(): Single<SongModel>
}