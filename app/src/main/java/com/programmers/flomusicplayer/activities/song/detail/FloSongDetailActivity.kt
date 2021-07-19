package com.programmers.flomusicplayer.activities.song.detail

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.programmers.flomusicplayer.FloApplication
import com.programmers.flomusicplayer.R
import com.programmers.flomusicplayer.databinding.ActivityFloSongDetailBinding
import com.programmers.flomusicplayer.models.LyricsItem
import com.programmers.flomusicplayer.models.SongItem
import com.programmers.flomusicplayer.viewmodels.FloSongDetailViewModel
import com.programmers.flomusicplayer.viewmodels.FloSongDetailViewModelFactory
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.io.IOException

class FloSongDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFloSongDetailBinding
    private lateinit var viewModel: FloSongDetailViewModel
    private val compositeDisposable = CompositeDisposable()

    private var mediaPlayer: MediaPlayer? = null
    private var errorSong: Boolean = false // 노래를 준비하다가 오류가 발생했는지 확인
    private lateinit var mHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // flo Repo
        val floRepository = (application as FloApplication).floRepository

        // View Model Create
        viewModel = ViewModelProvider(
            this,
            FloSongDetailViewModelFactory(floRepository, compositeDisposable)
        ).get(
            FloSongDetailViewModel::class.java
        )

        // ready to dataBinding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_flo_song_detail)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        setEventListener()
        loadAndShow()
    }

    override fun onStop() {
        super.onStop()

        // MediaPlayer Release
        mediaPlayer?.release()
        mediaPlayer = null

        // 데이터를 통신하다가 화면이 닫히면 => 통신 종료
        compositeDisposable.dispose()
    }

    private fun setEventListener() {
        // 노래 재생/일시정지 버튼
        binding.songDetailPlayButton.setOnCheckedChangeListener { buttonView, isChecked ->
            // https://stackoverflow.com/questions/45348820/using-return-inside-a-lambda
            // In Kotlin, return inside a lambda means return from the innermost nesting fun
            // (ignoring lambdas), and it is not allowed in lambdas that are not inlined

            // The return@setOnCheckedChangeListener syntax is used to specify the scope to return from.
            // You can use the name of the function the lambda is passed to as the label
            if (mediaPlayer == null) {
                Toast.makeText(applicationContext, R.string.not_ready_song, Toast.LENGTH_SHORT)
                    .show()
                return@setOnCheckedChangeListener
            }

            if (errorSong) {
                Toast.makeText(applicationContext, R.string.error_on_song, Toast.LENGTH_SHORT)
                    .show()
                return@setOnCheckedChangeListener
            }

            if (isChecked) {
                mediaPlayer?.start()
            } else {
                mediaPlayer?.pause()
            }
        }

        binding.songDetailSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (mediaPlayer == null) {
                    Toast.makeText(applicationContext, R.string.not_ready_song, Toast.LENGTH_SHORT)
                        .show()
                    return
                }

                if (errorSong) {
                    Toast.makeText(applicationContext, R.string.error_on_song, Toast.LENGTH_SHORT)
                        .show()
                    return
                }

                if (!fromUser) return

                mediaPlayer?.seekTo(progress * 1000)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        mediaPlayer?.setOnSeekCompleteListener {
            binding.songDetailPlayButton.isChecked = false
            mediaPlayer?.seekTo(0)
        }
    }

    /**
     * ViewModel에 데이터를 요청하고 Observer를 관리하는 함수
     */
    private fun loadAndShow() {
        // 노래 정보 요청
        viewModel.requestSong()

        // LiveData Song을 Observing 하고 화면에 표시
        viewModel.getLiveSong().observe(this, {
            // 노래 재생 준비
            prepareMediaPlayer(it)
            // 준비에서 오류가 없었다면 노래를 재생함
            if (!errorSong) binding.songDetailPlayButton.isChecked = true
        })

        // Repository에서 데이터를 가져올때 발생 한 실패 응답 핸들링
        viewModel.getLiveFailure()
            .observe(this, { Toast.makeText(applicationContext, it, Toast.LENGTH_SHORT).show() })

        // Repository 에서 데이터를 가져오다가 발생한 Expcetion 핸들링
        viewModel.getLiveError().observe(this, {
            // todo kbt : what to do
            Log.d("KBT", it.toString())
        })
    }

    /**
     * 전달받은 SongItem으로 MediaPlayer를 준비시키는 함수
     */
    private fun prepareMediaPlayer(song: SongItem) {
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )

            try {
//                throw IllegalArgumentException("접근 불가 테스트")
                setDataSource(song.fileUrl)
                prepare() // might take long! (for buffering, etc)
            } catch (e1: IllegalArgumentException) {
                errorSong = true
            } catch (e2: IOException) {
                errorSong = true
            }
        }

        if (errorSong) {
            binding.songDetailPlayButton.isEnabled = false // 재생 버튼을 누르지 못하게 함
            binding.songDetailPlayButton.alpha = 0.5f
            Toast.makeText(applicationContext, R.string.error_on_song, Toast.LENGTH_SHORT).show()
            return
        }

        syncMediaPlayer(song)
        viewModel.setCurrentTime(0)
        viewModel.setTotalTime(mediaPlayer?.duration ?: 0)
    }

    /**
     * MediaPlayer - Seekbar 동기화
     */
    private fun syncMediaPlayer(song: SongItem) {
        binding.songDetailSeekBar.max = (mediaPlayer?.duration ?: 0) / 1000
        mHandler = Handler(mainLooper)

        // 매 시간마다 MediaPlayer - Seekbar - Lyrics 를 동기화 한다
        val timeRunnable = object : Runnable {
            override fun run() {
                if (mediaPlayer != null) {
                    val lyricsList = song.lyricsList
                    val currentMilli = mediaPlayer?.currentPosition ?: 0
                    val currentSec = currentMilli / 1000

                    // 맞는 가사를 찾고 가사가 변경되었다면 업데이트
                    val position = findLyricsItemIndex(currentMilli, lyricsList)
                    if (position != -1) {
                        val changeLyrics = lyricsList[position].lyrics
                        if (viewModel.getLiveLyrics().value != changeLyrics) {
                            Log.d("KBT", "lyrics change!! ${lyricsList[position].lyrics}")
                            viewModel.setLyrics(lyricsList[position].lyrics)
                        }
                    }
                    
                    binding.songDetailSeekBar.progress = currentSec // Seekbar Progress 업데이트
                    viewModel.setCurrentTime(currentMilli) // 현재시간 업데이트
                    mHandler.postDelayed(this, 1000) // 재귀호출로 1초마다 동작 반복
                }
            }
        }

        runOnUiThread {
            mHandler.post(timeRunnable)
        }
    }

    /**
     * 현재 시간에 맞는 가사를 알려준다
     */
    private fun findLyricsItemIndex(currentTimeMillis: Int, lyricsList: List<LyricsItem>): Int {
        var lastIndex: Int = -1

        // indicies => IntRange 타입을 반환
        for (index in lyricsList.indices) {
            val item = lyricsList[index]
            val time = item.startTime.totalMillis()
            if (currentTimeMillis >= time) {
                lastIndex = index
            } else {
                break
            }
        }

        return lastIndex
    }
}