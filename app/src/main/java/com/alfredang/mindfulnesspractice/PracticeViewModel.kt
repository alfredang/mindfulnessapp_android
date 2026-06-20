package com.alfredang.mindfulnesspractice

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Mirrors the iOS `PracticePlayerViewModel`: a guided-voice [MediaPlayer] for the bundled `.m4a`
 * plus an optional looping background-music player (volume 0.22). Session timing is wall-clock
 * based over a 0.2s ticker, so a chosen [sessionLength] longer than the voice keeps the music
 * going to the end, and a shorter one cuts it off (auto-stop).
 */
class PracticeViewModel(app: Application) : AndroidViewModel(app) {

    var isPlaying by mutableStateOf(false)
        private set
    var currentTime by mutableDoubleStateOf(0.0)
        private set

    /** Total length of the session the user has chosen (drives the scrubber + auto-stop). */
    var sessionLength by mutableDoubleStateOf(0.0)
        private set

    /** Natural length of the guided voice track. */
    var voiceDuration by mutableDoubleStateOf(0.0)
        private set

    /** Title of the chosen background-music track, if any. */
    var musicTitle by mutableStateOf<String?>(null)
        private set
    var musicError by mutableStateOf<String?>(null)
        private set

    /** Length options (seconds) offered in the UI. */
    val lengthOptions: List<Double> = listOf(5 * 60.0, 10 * 60.0, 15 * 60.0, 20 * 60.0)

    // Build + prepare the voice player explicitly so audio attributes are set while the player
    // is still in the Initialized state (MediaPlayer.create() returns an already-prepared player).
    private val voicePlayer: MediaPlayer = MediaPlayer().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )
        val afd = app.resources.openRawResourceFd(R.raw.mindfulness_practice)
        afd.use { setDataSource(it.fileDescriptor, it.startOffset, it.length) }
        prepare()
    }
    private var musicPlayer: MediaPlayer? = null
    private var ticker: Job? = null
    private var anchorElapsedMs: Long = 0      // wall-clock anchor (elapsed at last start/seek)
    private var anchorClock: Long = 0

    init {
        voiceDuration = voicePlayer.duration / 1000.0
        // Default the session to the option closest to the guided voice length (~10 min).
        sessionLength = lengthOptions.minByOrNull { abs(it - voiceDuration) } ?: voiceDuration
    }

    // MARK: - Transport

    fun start() {
        if (currentTime >= sessionLength - 0.3) seek(0.0)
        if (currentTime < voiceDuration) voicePlayer.start()
        musicPlayer?.start()
        isPlaying = true
        anchorElapsedMs = (currentTime * 1000).toLong()
        anchorClock = SystemClock.elapsedRealtime()
        startTicker()
    }

    fun pause() {
        if (voicePlayer.isPlaying) voicePlayer.pause()
        musicPlayer?.takeIf { it.isPlaying }?.pause()
        isPlaying = false
        ticker?.cancel()
    }

    fun stop() {
        if (voicePlayer.isPlaying) voicePlayer.pause()
        musicPlayer?.takeIf { it.isPlaying }?.pause()
        ticker?.cancel()
        seek(0.0)
        isPlaying = false
    }

    fun seek(seconds: Double) {
        val clamped = seconds.coerceIn(0.0, sessionLength)
        currentTime = clamped
        val voicePos = minOf(clamped, maxOf(voiceDuration - 0.05, 0.0))
        runCatching { voicePlayer.seekTo((voicePos * 1000).toInt()) }
        anchorElapsedMs = (clamped * 1000).toLong()
        anchorClock = SystemClock.elapsedRealtime()
    }

    fun selectSessionLength(length: Double) {
        sessionLength = length
        if (currentTime > length) seek(length)
    }

    // MARK: - Background music

    fun setBackgroundMusic(title: String, uri: Uri?) {
        if (uri == null) {
            musicError = "“$title” can’t be used — pick a song stored on this device."
            return
        }
        musicError = null
        runCatching {
            val player = MediaPlayer().apply {
                setDataSource(getApplication(), uri)
                isLooping = true            // loop under the whole session
                setVolume(0.22f, 0.22f)     // sit gently beneath the voice
                prepare()
            }
            musicPlayer?.release()
            musicPlayer = player
            musicTitle = title
            if (isPlaying) player.start()
        }.onFailure {
            musicError = "Couldn’t play “$title”."
        }
    }

    fun clearBackgroundMusic() {
        musicPlayer?.release()
        musicPlayer = null
        musicTitle = null
    }

    // MARK: - Internals

    private fun startTicker() {
        ticker?.cancel()
        ticker = viewModelScope.launch {
            while (true) {
                tick()
                delay(200)
            }
        }
    }

    private fun tick() {
        if (!isPlaying) return
        val elapsed = anchorElapsedMs + (SystemClock.elapsedRealtime() - anchorClock)
        currentTime = minOf(elapsed / 1000.0, sessionLength)
        // Voice ends naturally before a longer session; music keeps looping until the end.
        if (currentTime >= sessionLength - 0.05) stop()
    }

    override fun onCleared() {
        super.onCleared()
        ticker?.cancel()
        voicePlayer.release()
        musicPlayer?.release()
    }
}
