package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class SoundEngine {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var enabled = true
    private var engineLoopJob: Job? = null
    private var engineTrack: AudioTrack? = null

    @Volatile
    private var engineRunning = false
    @Volatile
    private var engineRpm = 800f
    @Volatile
    private var hydraulicActive = false
    @Volatile
    private var reversing = false

    fun setEnabled(isEnabled: Boolean) {
        enabled = isEnabled
        if (!isEnabled) {
            stopEngineLoop()
        }
    }

    fun updateDrivingAudio(running: Boolean, rpm: Float, hydraulicMoving: Boolean, isReversing: Boolean) {
        engineRunning = running
        engineRpm = rpm.coerceIn(650f, 2300f)
        hydraulicActive = hydraulicMoving
        reversing = isReversing

        if (enabled && running) {
            if (engineLoopJob == null || engineLoopJob?.isActive != true) {
                startEngineLoop()
            }
        } else {
            stopEngineLoop()
        }
    }

    fun stopEngineLoop() {
        engineRunning = false
        engineLoopJob?.cancel()
        engineLoopJob = null
        try {
            engineTrack?.pause()
            engineTrack?.flush()
            engineTrack?.release()
        } catch (_: Throwable) {
        }
        engineTrack = null
    }

    private fun startEngineLoop() {
        engineLoopJob?.cancel()
        engineLoopJob = scope.launch {
            val sampleRate = 22050
            val chunkSamples = 1102 // ~50ms chunk
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(chunkSamples * 4)

            val track = try {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .setBufferSizeInBytes(minBuf)
                    .build()
            } catch (_: Throwable) {
                return@launch
            }

            engineTrack = track
            try {
                track.play()
            } catch (_: Throwable) {
                return@launch
            }

            val buffer = ShortArray(chunkSamples)
            var phase1 = 0.0
            var phase2 = 0.0
            var revTimer = 0f

            while (isActive && enabled && engineRunning) {
                val baseFreq = 26.0 + (engineRpm - 700.0) * 0.036 // Diesel 4-cylinder firing cadence
                val dt = 1.0 / sampleRate
                val hydNow = hydraulicActive
                val revNow = reversing

                for (i in 0 until chunkSamples) {
                    phase1 += 2.0 * PI * baseFreq * dt
                    phase2 += 2.0 * PI * (baseFreq * 2.0) * dt
                    // Diesel chug + subtle harmonic growl
                    var sample = 0.30 * sin(phase1) + 0.18 * sin(phase2) + 0.08 * sin(phase1 * 3.0)
                    // Slight diesel knock pulse
                    if (sin(phase1) > 0.7) sample += 0.08

                    if (hydNow) {
                        // Hydraulic pump whine + fluid hiss
                        val whine = sin(2.0 * PI * 420.0 * (i * dt + phase1 * 0.01))
                        val noise = (Random.nextDouble() - 0.5) * 0.14
                        sample += 0.12 * whine + noise
                    }

                    if (revNow) {
                        revTimer += dt.toFloat()
                        val cycle = revTimer % 0.72f
                        if (cycle < 0.34f) {
                            sample += 0.25 * sin(2.0 * PI * 1180.0 * revTimer)
                        }
                    } else {
                        revTimer = 0f
                    }

                    val clamped = (sample * 0.55).coerceIn(-0.95, 0.95)
                    buffer[i] = (clamped * Short.MAX_VALUE).toInt().toShort()
                }
                if (phase1 > 1000.0) phase1 %= (2.0 * PI)
                if (phase2 > 1000.0) phase2 %= (2.0 * PI)

                try {
                    track.write(buffer, 0, chunkSamples)
                } catch (_: Throwable) {
                    break
                }
                delay(2)
            }
        }
    }

    fun playRatchet() {
        if (!enabled) return
        playOneShot(durationMs = 220) { t, _ ->
            val click = if ((t * 28f).toInt() % 2 == 0) 1f else 0.2f
            val env = exp(-t * 14f) * click
            (sin(2.0 * PI * 920.0 * t) * 0.4 + (Random.nextDouble() - 0.5) * 0.5) * env
        }
    }

    fun playHammer() {
        if (!enabled) return
        playOneShot(durationMs = 260) { t, _ ->
            val env = exp(-t * 18f)
            (sin(2.0 * PI * 165.0 * t) * 0.6 + sin(2.0 * PI * 680.0 * t) * 0.35 + (Random.nextDouble() - 0.5) * 0.3) * env
        }
    }

    fun playHydraulicSnap() {
        if (!enabled) return
        playOneShot(durationMs = 240) { t, _ ->
            val env = exp(-t * 10f)
            (sin(2.0 * PI * 480.0 * t) * 0.25 + (Random.nextDouble() - 0.5) * 0.45) * env
        }
    }

    fun playSuccessChime() {
        if (!enabled) return
        playOneShot(durationMs = 320) { t, _ ->
            val freq = if (t < 0.14f) 587.33 else 880.0
            val env = exp(-t * 8f)
            sin(2.0 * PI * freq * t) * 0.45 * env
        }
    }

    fun playErrorBuzz() {
        if (!enabled) return
        playOneShot(durationMs = 220) { t, _ ->
            val env = exp(-t * 10f)
            (sin(2.0 * PI * 140.0 * t) * 0.45 + sin(2.0 * PI * 195.0 * t) * 0.35) * env
        }
    }

    fun playScannerBeep() {
        if (!enabled) return
        playOneShot(durationMs = 280) { t, _ ->
            val f = if ((t * 10f).toInt() % 2 == 0) 1320.0 else 1760.0
            sin(2.0 * PI * f * t) * 0.35 * exp(-t * 6f)
        }
    }

    fun playCollisionThud(impactSpeedMps: Float) {
        if (!enabled) return
        val gain = (impactSpeedMps / 4.5f).coerceIn(0.25f, 1.0f)
        playOneShot(durationMs = 140) { t, _ ->
            val env = exp(-t * 24f) * gain
            (sin(2.0 * PI * 115.0 * t) * 0.65 + sin(2.0 * PI * 320.0 * t) * 0.25 + (Random.nextDouble() - 0.5) * 0.25) * env
        }
    }

    private fun playOneShot(durationMs: Int, gen: (t: Float, i: Int) -> Double) {
        scope.launch {
            val sampleRate = 22050
            val count = (sampleRate * durationMs) / 1000
            val samples = ShortArray(count)
            for (i in 0 until count) {
                val t = i.toFloat() / sampleRate
                val v = gen(t, i).coerceIn(-0.95, 0.95)
                samples[i] = (v * Short.MAX_VALUE).toInt().toShort()
            }
            try {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .setBufferSizeInBytes(samples.size * 2)
                    .build()
                track.write(samples, 0, samples.size)
                track.play()
                delay(durationMs.toLong() + 60L)
                track.release()
            } catch (_: Throwable) {
            }
        }
    }
}
