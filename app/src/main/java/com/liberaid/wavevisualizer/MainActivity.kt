package com.liberaid.wavevisualizer

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val wr = WaveReader()
        wr.readWaveHeaderFromAssets(applicationContext, "example.wav")

        val valid = wr.isHeaderValid()
        val pcm = wr.isPCMFormat()

        if(!valid || !pcm)
            throw RuntimeException("Valid: $valid, pcm: $pcm")

        val channels = 2
        val bitsPerSample = 16

        val buffer = ShortArray(4) { 0.toShort() }
        val bytesRead = wr.readRaw16bit(buffer)

        Log.d("WR_BUFFER", "$bytesRead")
    }
}
