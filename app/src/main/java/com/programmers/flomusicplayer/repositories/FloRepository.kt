package com.programmers.flomusicplayer.repositories

import com.programmers.flomusicplayer.models.SongModel
import io.reactivex.rxjava3.disposables.Disposable

interface FloRepository {
    fun getSong(callback: BaseResponse<SongModel>): Disposable
}