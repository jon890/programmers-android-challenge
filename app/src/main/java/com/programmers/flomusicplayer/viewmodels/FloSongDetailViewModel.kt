package com.programmers.flomusicplayer.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.programmers.flomusicplayer.models.SongItem
import com.programmers.flomusicplayer.models.SongModel
import com.programmers.flomusicplayer.models.mapToPresentation
import com.programmers.flomusicplayer.repositories.BaseResponse
import com.programmers.flomusicplayer.repositories.FloRepositoryImpl
import io.reactivex.rxjava3.disposables.CompositeDisposable

class FloSongDetailViewModel(
    private val repositoryImpl: FloRepositoryImpl,
    private val compositeDisposable: CompositeDisposable
) : ViewModel() {

    private var liveSong: MutableLiveData<SongItem> = MutableLiveData()
    private var liveLoadingVisible: MutableLiveData<Boolean> = MutableLiveData()
    private var liveFailure: MutableLiveData<String> = MutableLiveData()
    private var liveError: MutableLiveData<Throwable> = MutableLiveData()

    private var liveLyrics: MutableLiveData<String> = MutableLiveData("")
    private var liveCurrentTime : MutableLiveData<Int> = MutableLiveData()
    private var liveTotalTime : MutableLiveData<Int> = MutableLiveData()

    fun requestSong() {
        repositoryImpl.getSong(object : BaseResponse<SongModel> {
            override fun onSuccess(data: SongModel) {
                liveSong.postValue(data.mapToPresentation())
            }

            override fun onFail(description: String) {
                liveFailure.postValue(description)
            }

            override fun onError(throwable: Throwable) {
                // todo kbt : 처리가능한 throwable 인가?
                liveError.postValue(throwable)
            }

            override fun onLoading() {
                liveLoadingVisible.postValue(true)
            }

            override fun onLoaded() {
                liveLoadingVisible.postValue(false)
            }
        }).also { compositeDisposable.add(it) }
    }

    fun getLiveSong(): LiveData<SongItem> {
        return liveSong
    }

    fun getLiveFailure(): LiveData<String> {
        return liveFailure
    }

    fun getLiveError(): LiveData<Throwable> {
        return liveError
    }

    fun getLiveLoadingVisible(): LiveData<Boolean> {
        return liveLoadingVisible
    }

    fun getLiveLyrics() : LiveData<String> {
        return liveLyrics
    }

    fun getLiveCurrentTime(): LiveData<Int> {
        return liveCurrentTime
    }

    fun getLiveTotalTime(): LiveData<Int> {
        return liveTotalTime
    }

    fun setLyrics(lyrics: String) {
        liveLyrics.postValue(lyrics)
    }

    fun setCurrentTime(timeMillis: Int) {
        liveCurrentTime.postValue(timeMillis)
    }

    fun setTotalTime(timeMillis: Int) {
        liveTotalTime.postValue(timeMillis)
    }
}

class FloSongDetailViewModelFactory(
    private val repositoryImpl: FloRepositoryImpl,
    private val compositeDisposable: CompositeDisposable
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FloSongDetailViewModel::class.java)) {
            return FloSongDetailViewModel(repositoryImpl, compositeDisposable) as T
        }
        throw IllegalAccessException("Unknown ViewModel Class")
    }
}