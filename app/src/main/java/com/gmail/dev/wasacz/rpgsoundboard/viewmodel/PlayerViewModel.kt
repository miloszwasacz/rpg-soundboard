package com.gmail.dev.wasacz.rpgsoundboard.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.gmail.dev.wasacz.rpgsoundboard.model.LocalPlaylist
import com.gmail.dev.wasacz.rpgsoundboard.model.LocalSong
import com.gmail.dev.wasacz.rpgsoundboard.model.Playlist
import com.gmail.dev.wasacz.rpgsoundboard.model.Song
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val player = ExoPlayer.Builder(getApplication()).build()
    var currentPlaylist by mutableStateOf<Playlist?>(null)
        private set
    var isPlaying by mutableStateOf(false)
        private set
    private var currentEndListener: Player.Listener? = null
    //private var currentSongIndex = 0

    fun setUpPlayer() {
        isPlaying = false
        player.apply {
            stop()
            clearMediaItems()
        }
        currentEndListener?.let { player.removeListener(it) }
        currentPlaylist = null
        //currentSongIndex = 0
    }

    private fun createEndListener(playlist: LocalPlaylist): Player.Listener {
        return object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when(playbackState) {
                    Player.STATE_ENDED -> playPlaylist(playlist)
                    else -> super.onPlaybackStateChanged(playbackState)
                }
            }
        }
    }

    fun playSong(context: Context, song: Song) {
        if (song is LocalSong) {
            player.stop()
            player.clearMediaItems()
            player.addMediaItem(MediaItem.fromUri(song.getUri()))
            player.prepare()
            player.play()
        }
    }

    fun playPlaylist(playlist: Playlist) {
        isPlaying = false
        //currentSongIndex = 0
        player.apply {
            stop()
            clearMediaItems()
            if (playlist is LocalPlaylist) {
                val listener = createEndListener(playlist)
                this@PlayerViewModel.currentEndListener = listener
                addListener(listener)
                addMediaItems(playlist.songList.shuffled().map { MediaItem.fromUri(it.getUri()) })
                prepare()
                play()
            }
        }
        isPlaying = true
        currentPlaylist = playlist
    }

    fun resume() {
        player.play()
        isPlaying = true
    }

    fun pause() {
        isPlaying = false
        player.pause()
    }

    fun stop() {
        player.apply {
            stop()
            clearMediaItems()
        }
        isPlaying = false
        currentPlaylist = null
        //currentSongIndex = 0
    }

    fun next() {
        player.seekToNext()
        resume()
    }

    fun previous() {
        player.seekToPreviousMediaItem()
        resume()
    }

    override fun onCleared() {
        player.apply {
            stop()
            release()
        }
        super.onCleared()
    }
}
