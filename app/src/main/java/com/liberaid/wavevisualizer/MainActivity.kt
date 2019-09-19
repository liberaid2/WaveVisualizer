package com.liberaid.wavevisualizer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val wr = WaveReader()
        wr.readWaveHeaderFromAssets(applicationContext, "example.wav")

        val valid = wr.isHeaderValid()
        val pcm = wr.isPCMFormat()
    }
}
