package com.programmers.flomusicplayer

import android.app.Application
import com.programmers.flomusicplayer.network.ApiProvider
import com.programmers.flomusicplayer.repositories.FloRepositoryImpl

class FloApplication: Application() {
    // Repository는 Application에 선언하여 Application 전역으로 공유하여 사용
    val floRepository = FloRepositoryImpl(ApiProvider.provideFloApi())
}