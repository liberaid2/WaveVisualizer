package com.liberaid.wavevisualizer

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

class WaveReader {

    private var chunkId = 0
    private var chunkSize = 0
    private var format = 0
    private var subchunk1Id = 0
    private var subchunk1Size = 0
    private var audioFormat: Short = 0
    private var numChannels: Short = 0
    private var sampleRate = 0
    private var byteRate = 0
    private var blockAlign: Short = 0
    private var bitsPerSample: Short = 0
    private var subchunk2Id = 0
    private var subchunk2Size = 0

    private val charBuff = CharArray(4)

    private var reader: BufferedReader? = null
    private var isHeaderRead = false

    fun readWaveHeaderFromAssets(context: Context, filename: String) {
        val isr = InputStreamReader(context.assets.open(filename))
        reader = BufferedReader(isr)

        isHeaderRead = readHeader()
    }

    private fun readHeader(): Boolean {
        val reader = reader ?: return false

        /* chunkId */
        if(reader.read(charBuff) != 4)
            return false

        chunkId = charBuffToInt()

        /* chunkSize */
        if(reader.read(charBuff) != 4)
            return false

        chunkSize = charBuffToInt()

        /* format */
        if(reader.read(charBuff) != 4)
            return false

        format = charBuffToInt()

        /* subchunk1Id */
        if(reader.read(charBuff) != 4)
            return false

        subchunk1Id = charBuffToInt()

        /* subchunk1Size */
        if(reader.read(charBuff) != 4)
            return false

        subchunk1Size = charBuffToInt()

        /* audioFormat & numChannels */
        if(reader.read(charBuff) != 4)
            return false

        charBuffToShots().also {(af, nc) ->
            audioFormat = af
            numChannels = nc
        }

        /* sampleRate */
        if(reader.read(charBuff) != 4)
            return false

        sampleRate = charBuffToInt()

        /* byteRate */
        if(reader.read(charBuff) != 4)
            return false

        byteRate = charBuffToInt()

        /* blockAlign & bitsPerSample */
        if(reader.read(charBuff) != 4)
            return false

        charBuffToShots().also { (ba, bps) ->
            blockAlign = ba
            bitsPerSample = bps
        }

        /* subchunk2Id */
        if(reader.read(charBuff) != 4)
            return false

        subchunk2Id = charBuffToInt()

        /* subchunk2Size */
        if(reader.read(charBuff) != 4)
            return false

        subchunk2Size = charBuffToInt()

        return true
    }

    fun isHeaderValid(): Boolean {
        if(!isHeaderRead)
            throw unreadWaveHeaderException

        return chunkId == MAGIC_RIFF && format == MAGIC_WAVE && subchunk1Id == MAGIC_FMT && subchunk2Id == MAGIC_DATA
    }

    fun isPCMFormat(): Boolean = subchunk1Size == 16 && audioFormat == 1.toShort()

    private var counter = 0

    private fun charBuffToInt(): Int {
        val a = charBuff[0].toInt()
        val b = charBuff[1].toInt()
        val c = charBuff[2].toInt()
        val d = charBuff[3].toInt()

        return (d shl 24) or (c shl 16) or (b shl 8) or a
    }

    private fun charBuffToShots(): Pair<Short, Short> {
        val a = charBuff[0].toInt()
        val b = charBuff[1].toInt()
        val c = charBuff[2].toInt()
        val d = charBuff[3].toInt()

        return ((b shl 8) or a).toShort() to ((d shl 8) or c).toShort()
    }

    companion object {
        const val MAGIC_RIFF = 0x46464952
        const val MAGIC_WAVE = 0x45564157
        const val MAGIC_FMT = 0x20746D66
        const val MAGIC_DATA = 0x61746164

        val unreadWaveHeaderException = UnreadWaveHeaderException()

        class UnreadWaveHeaderException() : RuntimeException("Unread wave header or an error occurred during reading")
    }
}