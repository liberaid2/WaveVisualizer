package com.liberaid.wavevisualizer

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

class WaveReader {

    var chunkId = 0; private set
    var chunkSize = 0; private set
    var format = 0; private set
    var subchunk1Id = 0; private set
    var subchunk1Size = 0; private set
    var audioFormat: Short = 0; private set
    var numChannels: Short = 0; private set
    var sampleRate = 0; private set
    var byteRate = 0; private set
    var blockAlign: Short = 0; private set
    var bitsPerSample: Short = 0; private set
    var subchunk2Id = 0; private set
    var subchunk2Size = 0; private set

    private val charBuff = CharArray(4)
    private val rawCharBuff = CharArray(32 * 100)

    private var reader: BufferedReader? = null
    private var isHeaderRead = false
    private var bytesRead = 0

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

        bytesRead += 4
        chunkId = charBuffToInt()

        /* chunkSize */
        if(reader.read(charBuff) != 4)
            return false

        bytesRead += 4
        chunkSize = charBuffToInt()

        /* format */
        if(reader.read(charBuff) != 4)
            return false

        bytesRead += 4
        format = charBuffToInt()

        /* subchunk1Id */
        if(reader.read(charBuff) != 4)
            return false

        bytesRead += 4
        subchunk1Id = charBuffToInt()

        /* subchunk1Size */
        if(reader.read(charBuff) != 4)
            return false

        bytesRead += 4
        subchunk1Size = charBuffToInt()

        /* audioFormat & numChannels */
        if(reader.read(charBuff) != 4)
            return false

        bytesRead += 4
        charBuffToShots().also {(af, nc) ->
            audioFormat = af
            numChannels = nc
        }

        /* sampleRate */
        if(reader.read(charBuff) != 4)
            return false

        bytesRead += 4
        sampleRate = charBuffToInt()

        /* byteRate */
        if(reader.read(charBuff) != 4)
            return false

        bytesRead += 4
        byteRate = charBuffToInt()

        /* blockAlign & bitsPerSample */
        if(reader.read(charBuff) != 4)
            return false

        bytesRead += 4
        charBuffToShots().also { (ba, bps) ->
            blockAlign = ba
            bitsPerSample = bps
        }

        /* subchunk2Id */
        if(reader.read(charBuff) != 4)
            return false

        bytesRead += 4
        subchunk2Id = charBuffToInt()

        /* subchunk2Size */
        if(reader.read(charBuff) != 4)
            return false

        bytesRead += 4
        subchunk2Size = charBuffToInt()

        return true
    }

    fun isHeaderValid(): Boolean {
        if(!isHeaderRead)
            throw unreadWaveHeaderException

        return chunkId == MAGIC_RIFF && format == MAGIC_WAVE && subchunk1Id == MAGIC_FMT && subchunk2Id == MAGIC_DATA
    }

    fun isPCMFormat(): Boolean = subchunk1Size == 16 && audioFormat == 1.toShort()

    fun readRaw8bit(buffer: CharArray): Int{
        if(bitsPerSample != 8.toShort())
            throw WrongDepthReading(8, bitsPerSample)

        return readRaw(buffer, buffer.size)
    }

    fun readRaw16bit(buffer: ShortArray): Int {
        if(bitsPerSample != 16.toShort())
            throw WrongDepthReading(16, bitsPerSample)

        val needBytes = buffer.size * 2
        var currentBytes = 0
        var currentReadBytes = 0
        var currentPos = 0

        do {
            val toRead = Math.min(needBytes - currentBytes, rawCharBuff.size)
            currentReadBytes = readRaw(rawCharBuff, toRead)
            currentBytes += currentReadBytes

            for(i in 0 until currentReadBytes){
                if(i % 2 == 0)
                    continue

                val curr = rawCharBuff[i].toInt()
                val prev = rawCharBuff[i - 1].toInt()

                buffer[currentPos++] = ((curr shl 8) and prev).toShort()
            }

        } while(currentBytes != needBytes && currentReadBytes != 0)

        return currentBytes
    }

    fun readRaw(buffer: CharArray, length: Int): Int {
        val reader = reader ?: throw unreadWaveHeaderException
        if(length > buffer.size)
            throw RuntimeException("Requested length ($length) is greater than buffer length (${buffer.size})")

        val read = reader.read(buffer, bytesRead, Math.min(buffer.size, length))
        bytesRead += read
        return read
    }

    fun close() {
        bytesRead = 0
        reader?.close()
        reader = null
    }

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

    }

    class UnreadWaveHeaderException : IllegalStateException("Unread wave header or an error occurred during reading")
    class WrongDepthReading(tryDepth: Int, actualDepth: Short) : RuntimeException("Trying to read raw wave with wrong depth, attempt=$tryDepth, actual=$actualDepth")
}