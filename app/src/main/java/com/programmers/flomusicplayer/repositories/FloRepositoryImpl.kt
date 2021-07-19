package com.programmers.flomusicplayer.repositories

import com.programmers.flomusicplayer.models.SongModel
import com.programmers.flomusicplayer.network.FloRestful
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import retrofit2.HttpException

class FloRepositoryImpl(
    private val floApi: FloRestful
) : FloRepository {

    override fun getSong(callback: BaseResponse<SongModel>): Disposable {
        return floApi.getSong()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                callback.onLoading()
            }
            .doOnTerminate {
                callback.onLoaded()
            }
            .subscribe({
                callback.onSuccess(it)
            }) {
                if (it is HttpException) {
                    callback.onFail(it.message())
                } else {
                    callback.onError(it)
                }
            }
    }
}