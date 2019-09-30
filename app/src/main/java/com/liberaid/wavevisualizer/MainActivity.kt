package com.liberaid.wavevisualizer

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnStart.setOnClickListener {
            val path = getExternalFilesDir(null)!!
            waveVisualizer.loadWaveFromFile("$path/example.wav")
            Toast.makeText(this@MainActivity, "Loaded", Toast.LENGTH_SHORT).show()
        }
    }
}
