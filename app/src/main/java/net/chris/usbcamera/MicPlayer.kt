package net.chris.usbcamera

import android.media.*
import android.media.AudioFormat.*
import android.media.AudioRecord.STATE_INITIALIZED
import android.media.MediaRecorder.AudioSource
import android.os.Build
import timber.log.Timber


class MicPlayer {

    private var audioTrack: AudioTrack? = null
    private var audioRecord: AudioRecord? = null
    private var isPlaying = false

    fun release() {
        audioTrack?.release()
        audioRecord?.release()
    }

    fun stop() {
        isPlaying = false
        audioTrack?.stop()
        audioRecord?.stop()
    }

    fun start() {
//        stop()
        Thread(Runnable { play() }).start()
    }

    private fun play() {
        val recBufSize = init()
        isPlaying = true
        val recBuf = ByteArray(recBufSize)
        audioRecord?.startRecording()
        audioTrack?.play()
        while (isPlaying) {
            val readLen = audioRecord?.read(recBuf, 0, recBufSize) ?: 0
            audioTrack?.write(recBuf, 0, readLen)
        }
    }

    private fun init(): Int {
        val recBufSize = AudioRecord.getMinBufferSize(
            FREQUENCY,
            CHANNEL_CONFIGURATION_IN,
            AUDIO_ENCODING
        ) * 2

        val plyBufSize = AudioTrack.getMinBufferSize(
            FREQUENCY,
            CHANNEL_CONFIGURATION_OUT,
            AUDIO_ENCODING
        ) * 2

        audioRecord = AudioRecord(
            AudioSource.MIC,
            FREQUENCY,
            CHANNEL_CONFIGURATION_IN,
            AUDIO_ENCODING,
            recBufSize
        )

        audioTrack =
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AUDIO_ENCODING)
                            .setSampleRate(FREQUENCY)
                            .setChannelMask(CHANNEL_CONFIGURATION_OUT)
                            .build()
                    )
                    .setBufferSizeInBytes(plyBufSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            else
                AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    FREQUENCY,
                    CHANNEL_CONFIGURATION_OUT,
                    AUDIO_ENCODING,
                    plyBufSize,
                    AudioTrack.MODE_STREAM
                ).apply {
                    playbackRate = FREQUENCY
                }
        return recBufSize
    }

    fun findAudioRecord(): AudioRecord? {
        var record: AudioRecord? = null
        for (rate in intArrayOf(48000, 44100, 22050, 11025, 16000, 8000)) {
            for (audioFormat in intArrayOf(ENCODING_PCM_8BIT, ENCODING_PCM_16BIT)) {
                for (channelConfig in intArrayOf(CHANNEL_IN_MONO, CHANNEL_IN_STEREO)) {
                    try {
                        val bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat)
                        Timber.d("Attempting ${rate}Hz: bits:$audioFormat channel:$channelConfig")
                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                            // check if we can instantiate and have a success
                            val recorder = AudioRecord(
                                AudioSource.MIC,
                                rate,
                                channelConfig,
                                audioFormat,
                                bufferSize
                            )

                            when (recorder.state) {
                                STATE_INITIALIZED -> {
                                    Timber.d("INITIALIZED ${recorder.state}")
                                    record = recorder
                                }
                                else ->
                                    Timber.w("STATE_UNINITIALIZED ${recorder.state}")
                            }
                        }
                    } catch (e: Exception) {
                        Timber.e(e, rate.toString() + "Exception, keep trying.")
                    }
                }
            }
        }
        record?.apply {
            release()
        } ?: Timber.e(" No recorder found!")
        return record
    }

    companion object {
        const val FREQUENCY = 44100
        const val CHANNEL_CONFIGURATION_IN = CHANNEL_IN_MONO
        const val CHANNEL_CONFIGURATION_OUT = CHANNEL_OUT_MONO
        const val AUDIO_ENCODING = ENCODING_PCM_16BIT
    }
}