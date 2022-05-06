package com.gmail.dev.wasacz.rpgsoundboard.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.gmail.dev.wasacz.rpgsoundboard.model.PlaylistType
import com.gmail.dev.wasacz.rpgsoundboard.model.SongType
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.LocalPlaylist
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.LocalSong
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Playlist
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Song
import java.io.IOException

class MediaPlayerService : Service(), MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,
    AudioManager.OnAudioFocusChangeListener {
    inner class LocalBinder : Binder() {
        fun getService(): MediaPlayerService = this@MediaPlayerService
    }

    companion object {
        private val ErrorMap = mapOf(
            MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK to "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK",
            MediaPlayer.MEDIA_ERROR_SERVER_DIED to "MEDIA_ERROR_SERVER_DIED",
            MediaPlayer.MEDIA_ERROR_UNKNOWN to "MEDIA_ERROR_UNKNOWN"
        )
    }

    private val binder: IBinder = LocalBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var mediaFiles: List<Song>? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var resumePosition: Int? = null
    private var currentSong: Int = 0

    fun play(playlist: Playlist) {
        if (!requestAudioFocus()) stopSelf()
        when (playlist.type) {
            PlaylistType.LOCAL -> {
                playlist as LocalPlaylist
                currentSong = 0
                mediaFiles = playlist.songList
                mediaPlayer?.stop()
                playNextTrack()
            }
            PlaylistType.SPOTIFY -> TODO("Not yet implemented - Spotify song")
        }
    }

    fun pause() {
        pauseMedia(mediaPlayer)
    }

    fun resume() {
        resumeMedia(mediaPlayer)
    }

    private fun initMediaPlayer() {
        also {
            mediaPlayer = MediaPlayer().apply {
                setOnCompletionListener(it)
                setOnErrorListener(it)
                setOnPreparedListener(it)
                setOnBufferingUpdateListener(it)
                setOnSeekCompleteListener(it)
                setOnInfoListener(it)
            }
        }
    }

    private fun playNextTrack() {
        val size = mediaFiles?.size ?: 0
        if (size > 0 && currentSong in 0 until size) {
            val song = mediaFiles!![currentSong]
            when (song.type) {
                SongType.LOCAL -> {
                    song as LocalSong
                    mediaPlayer?.apply {
                        reset()
                        setAudioAttributes(AudioAttributes.Builder().run {
                            setUsage(AudioAttributes.USAGE_MEDIA)
                            setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            build()
                        })
                        try {
                            setDataSource(applicationContext, song.uri)
                        } catch (e: IOException) {
                            stopSelf()
                        }
                        prepareAsync()
                    }
                }
            }
        } else {
            mediaFiles = null
            currentSong = 0
            removeAudioFocus()
        }
    }

    private fun playMedia(player: MediaPlayer?) {
        player?.run {
            if (!isPlaying)
                start()
        }
    }

    private fun stopMedia(player: MediaPlayer?) {
        player?.run {
            if (isPlaying)
                stop()
        }
    }

    private fun pauseMedia(player: MediaPlayer?) {
        player?.run {
            if (isPlaying) {
                pause()
                resumePosition = currentPosition
            }
        }
    }

    private fun resumeMedia(player: MediaPlayer?) {
        player?.run {
            if (!isPlaying) {
                seekTo(resumePosition ?: 0)
                start()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        if (mediaPlayer == null) initMediaPlayer()
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        stopMedia(mediaPlayer)
        return super.onUnbind(intent)
    }

    override fun onCompletion(mp: MediaPlayer?) {
        stopMedia(mp)
        if (mediaFiles != null) {
            currentSong++
            playNextTrack()
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        playMedia(mp)
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        val message = ErrorMap[what]
        Log.d("MediaPlayer Error", "${message ?: "Error no.: $what"} - $extra")
        return false
    }

    override fun onSeekComplete(mp: MediaPlayer?) {}

    override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean = true

    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {}

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!requestAudioFocus()) stopSelf()
        else initMediaPlayer()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (mediaPlayer == null) initMediaPlayer()
                else playMedia(mediaPlayer)
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                stopMedia(mediaPlayer)
                mediaPlayer?.release()
                mediaPlayer = null
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pauseMedia(mediaPlayer)
        }
    }

    private fun requestAudioFocus(): Boolean {
        var result: Int
        audioManager = getSystemService(Context.AUDIO_SERVICE).let {
            if (it is AudioManager) {
                audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                    setAudioAttributes(AudioAttributes.Builder().run {
                        setUsage(AudioAttributes.USAGE_GAME)
                        setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        build()
                    })
                    setOnAudioFocusChangeListener(this@MediaPlayerService)
                    build()
                }
                result = it.requestAudioFocus(audioFocusRequest!!)
                it
            } else {
                result = AudioManager.AUDIOFOCUS_REQUEST_FAILED
                null
            }
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun removeAudioFocus(): Boolean {
        val result = audioFocusRequest?.let {
            audioManager?.abandonAudioFocusRequest(it) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } ?: false
        audioFocusRequest = null
        return result
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMedia(mediaPlayer)
        mediaPlayer?.release()
        stopSelf()
        removeAudioFocus()
    }
}