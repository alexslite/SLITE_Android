package com.sliteptyltd.slite.utils.bluetooth.update

import android.annotation.SuppressLint
import android.content.Context
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.data.model.version.Version
import com.sliteptyltd.slite.utils.logMe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

typealias OnChunkReadyForWriting = (ByteArray) -> Unit
typealias OnUpdateProgressChanged = (Int) -> Unit
typealias OnAllChunksWritten = () -> Unit

private typealias OnChunksLoaded = (List<ByteArray>) -> Unit

class UpdateProvider(private val context: Context) {
    private var lastWrittenChunk = -1
    private var totalChunksToWrite = 0
    private var totalChunksWritten = 0
    private var isUpdateProcessActive = false

    private var onChunkReadyForWriting: OnChunkReadyForWriting? = null
    private var onAllChunksWritten: OnAllChunksWritten? = null

    private lateinit var firmwareChunks: List<ByteArray>

    private val updateScope = CoroutineScope(Dispatchers.IO)

    fun startUpdateProcess(onChunkReadyForWriting: OnChunkReadyForWriting, onAllChunksWritten: OnAllChunksWritten) {
        if (isUpdateProcessActive) return

        isUpdateProcessActive = true
        activeFirmwareUpdates++

        this.onChunkReadyForWriting = onChunkReadyForWriting
        this.onAllChunksWritten = onAllChunksWritten

        loadFirmwareChunks { chunks ->
            firmwareChunks = chunks
            totalChunksToWrite = chunks.size
            enqueueNextChunkForWriting()
        }
    }

    fun onChunkWritten(onUpdateProgressChanged: OnUpdateProgressChanged) {
        totalChunksWritten++

        logMe { "[Update] Chunk written! Total written: $totalChunksWritten left: ${totalChunksToWrite - totalChunksWritten}" }

        val progress = (totalChunksWritten.toFloat() / totalChunksToWrite * 100).roundToInt()
        onUpdateProgressChanged.invoke(progress)

        // Safety check in case the firmware changes over time and the last chunks is reported as written
        if (totalChunksWritten == totalChunksToWrite) {
            onUpdateComplete()
        } else {
            enqueueNextChunkForWriting()
        }
    }

    private fun clearUp() {
        lastWrittenChunk = -1
        totalChunksWritten = 0

        isUpdateProcessActive = false
        activeFirmwareUpdates--

        // Remove cache if there's no other update happening
        if (activeFirmwareUpdates == 0) {
            cachedFirmwareChunks = null
        }

        onChunkReadyForWriting = null
        onAllChunksWritten = null
    }

    /**
     * Send chunks one by one in order to avoid putting pressure on the BLE API
     */
    private fun enqueueNextChunkForWriting() {
        onChunkReadyForWriting?.invoke(firmwareChunks[++lastWrittenChunk])

        // We won't receive the last byte confirmation as the device restarts instantly
        if (lastWrittenChunk == totalChunksToWrite - 1) {
            onUpdateComplete()
        }
    }

    private fun onUpdateComplete() {
        onAllChunksWritten?.invoke()
        logMe { "[Update] Update complete!" }
        clearUp()
    }

    private fun loadFirmwareChunks(onChunksLoaded: OnChunksLoaded) {
        cachedFirmwareChunks?.let { chunks ->
            onChunksLoaded(chunks)
            return
        }

        updateScope.launch(Dispatchers.IO) {
            val firmwareInputStream = context.resources.openRawResource(LATEST_FIRMWARE_RESOURCE)
            val firmwareChunks = mutableListOf<ByteArray>()
            val buffer = ByteArray(UPDATE_CHUNK_SIZE_BYTES)
            var length: Int
            var lastPackageLength = -1

            firmwareInputStream.use { inputStream ->
                while (inputStream.read(buffer).apply { length = this } > 0) {
                    firmwareChunks.add(buffer.copyOf(length))
                    lastPackageLength = length
                }
            }

            // If final length is UPDATE_CHUNK_SIZE_BYTES, we need to add another empty package
            // The device identifies the last package as being non UPDATE_CHUNK_SIZE_BYTES in size, so we need to add an empty one
            if (lastPackageLength == UPDATE_CHUNK_SIZE_BYTES) {
                firmwareChunks.add(ByteArray(0))
            }

            cachedFirmwareChunks = firmwareChunks

            launch(Dispatchers.Main) {
                onChunksLoaded.invoke(firmwareChunks)
            }
        }
    }

    companion object {
        val LATEST_AVAILABLE_SOFTWARE_VERSION = Version.parseFromString("2.8.5")

        private var cachedFirmwareChunks: List<ByteArray>? = null
        private var activeFirmwareUpdates = 0

        @SuppressLint("NonConstantResourceId")
        private const val LATEST_FIRMWARE_RESOURCE = R.raw.slite_2_8_5
        private const val UPDATE_CHUNK_SIZE_BYTES = 512

        fun isUpdateAvailableForVersion(softwareVersion: Version): Boolean {
            return LATEST_AVAILABLE_SOFTWARE_VERSION > softwareVersion
        }
    }
}