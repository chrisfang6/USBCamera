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
        if (audioRecord != null && audioTrack != null) {
            isPlaying = true
            val recBuf = ByteArray(recBufSize)
            audioRecord?.startRecording()
            audioTrack?.play()
            while (isPlaying) {
                val readLen = audioRecord?.read(recBuf, 0, recBufSize) ?: 0
                audioTrack?.write(recBuf, 0, readLen)
            }
        }
    }

    private fun init(): Int {
//        val recBufSize = AudioRecord.getMinBufferSize(
//            FREQUENCY,
//            CHANNEL_CONFIGURATION_IN,
//            AUDIO_ENCODING
//        ) * FACTOR

        val plyBufSize = AudioTrack.getMinBufferSize(
            FREQUENCY,
            CHANNEL_CONFIGURATION_OUT,
            AUDIO_ENCODING
        ) * FACTOR

        val (record, recBufSize) = findAudioRecord()
        audioRecord = record
        /*AudioRecord(
            AudioSource.MIC,
            FREQUENCY,
            CHANNEL_CONFIGURATION_IN,
            AUDIO_ENCODING,
            recBufSize
        )*/

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

    private fun findAudioRecord(): AudioRecordInfo {
        for (rate in intArrayOf(FREQUENCY, 22050, 11025, 16000, 8000)) {
            for (audioFormat in intArrayOf(AUDIO_ENCODING, ENCODING_PCM_8BIT)) {
                for (channelConfig in intArrayOf(CHANNEL_CONFIGURATION_IN, CHANNEL_IN_STEREO)) {
                    try {
                        val bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat) * FACTOR
                        Timber.d("Attempting ${rate}Hz: bits:$audioFormat channel:$channelConfig")
                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                            // check if we can instantiate and have a success
                            val recorder = AudioRecord(
                                AudioSource.VOICE_COMMUNICATION,
                                rate,
                                channelConfig,
                                audioFormat,
                                bufferSize
                            )

                            when (recorder.state) {
                                STATE_INITIALIZED -> {
                                    Timber.d("INITIALIZED ${recorder.state}")
                                    return AudioRecordInfo(recorder, bufferSize)
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
        return AudioRecordInfo(null, 0)
    }

    companion object {
        const val FREQUENCY = 44100
        const val CHANNEL_CONFIGURATION_IN = CHANNEL_IN_MONO
        const val CHANNEL_CONFIGURATION_OUT = CHANNEL_OUT_MONO
        const val AUDIO_ENCODING = ENCODING_PCM_16BIT
        const val FACTOR = 2
    }

    data class AudioRecordInfo(val audioRecord: AudioRecord?, val bufferSize: Int)
}