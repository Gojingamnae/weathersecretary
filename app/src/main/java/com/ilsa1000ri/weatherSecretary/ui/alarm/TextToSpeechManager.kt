package com.ilsa1000ri.weatherSecretary.ui.alarm

// tts 관리 & 제어
import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.UUID

class TextToSpeechManager(context: Context, private val onInitialized: (Boolean) -> Unit,
                          val onSpeakCompleted: () -> Unit) {
    private var tts: TextToSpeech? = null

    private val utteranceId: String = UUID.randomUUID().toString()

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.KOREAN
                setVoice("ko-KR-Wavenet-A")
                setupListener()
                onInitialized(true)
            } else {
                onInitialized(false)
            }
        }
    }

    // 보이스 변환- 한가지 목소리만 출력되도록.
    private fun setVoice(voiceName: String) {
        val voice = tts?.voices?.find { it.name == voiceName }
        if (voice != null) {
            tts?.voice = voice
        }
    }

    private fun setupListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                // 음성 출력이 시작될 때 호출됩니다.
            }

            override fun onDone(utteranceId: String?) {
                // 음성 출력이 완료되면 호출됩니다.
                if (this@TextToSpeechManager.utteranceId == utteranceId) {
                    onSpeakCompleted()
                }
            }

            override fun onError(utteranceId: String?) {
                // 음성 출력 중 에러가 발생한 경우 호출됩니다.
            }
        })
    }

    fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
    }
}