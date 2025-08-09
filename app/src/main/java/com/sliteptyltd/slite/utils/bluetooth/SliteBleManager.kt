package com.sliteptyltd.slite.utils.bluetooth

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
import android.content.Context
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.COLORS
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.EFFECTS
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode.WHITE
import com.sliteptyltd.slite.data.model.version.SliteVersion
import com.sliteptyltd.slite.data.model.version.Version
import com.sliteptyltd.slite.utils.bluetooth.SliteLightCharacteristic.Companion.fromCharacteristicByteArray
import com.sliteptyltd.slite.utils.bluetooth.SliteLightCharacteristic.Companion.toCharacteristicCSV
import com.sliteptyltd.slite.utils.bluetooth.SliteLightCharacteristic.Companion.toEffectCharacteristicCSV
import com.sliteptyltd.slite.utils.bluetooth.update.UpdateProvider
import com.sliteptyltd.slite.utils.bluetooth.update.UpdateState
import com.sliteptyltd.slite.utils.logMe
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.data.Data
import org.koin.java.KoinJavaComponent
import java.util.UUID

typealias OnSliteOutputChanged = (SliteLightCharacteristic) -> Unit
typealias OnSliteConfigurationModeChanged = (LightConfigurationMode) -> Unit
typealias OnSliteUpdateStatusChanged = (UpdateState) -> Unit

class SliteBleManager(context: Context) : BleManager(context) {

    private var lightOutputControlPoint: BluetoothGattCharacteristic? = null
    private var sliteStatusControlPoint: BluetoothGattCharacteristic? = null
    private var sliteReadVersionControlPoint: BluetoothGattCharacteristic? = null
    private var sliteUpdateVersionControlPoint: BluetoothGattCharacteristic? = null
    private var onSliteOutputChanged: OnSliteOutputChanged? = null
    private var onSliteConfigurationModeChanged: OnSliteConfigurationModeChanged? = null
    private var onSliteUpdateStatusChanged: OnSliteUpdateStatusChanged? = null
    private var ignoreOutputNotification = false
    private var ignoreModeAndStatusNotification = false
    private var updateState: UpdateState = UpdateState.NotAvailable(Version.ZERO)
    private val updateProvider: UpdateProvider by lazy { KoinJavaComponent.get(UpdateProvider::class.java) }

    override fun getGattCallback(): BleManagerGattCallback = SliteGattCallback()

    override fun log(priority: Int, message: String) {
        logMe { message }
    }

    fun readLightOutput() {
        readCharacteristic(lightOutputControlPoint)
            .with { _, data ->
                val characteristicValue = data.value ?: return@with
                onSliteOutputChanged?.invoke(fromCharacteristicByteArray(characteristicValue))
            }
            .enqueue()
    }

    fun writeLightOutput(
        sliteLightCharacteristic: SliteLightCharacteristic,
        ignoreBlackout: Boolean = false,
        ignoreWriteOutputNotification: Boolean = true
    ) {
        val sliteCharacteristicCSV = if (ignoreBlackout) {
            sliteLightCharacteristic.toEffectCharacteristicCSV()
        } else {
            sliteLightCharacteristic.toCharacteristicCSV()
        }

        writeCharacteristic(lightOutputControlPoint, Data.from(sliteCharacteristicCSV), WRITE_TYPE_NO_RESPONSE)
            .done {
                if (ignoreWriteOutputNotification) {
                    ignoreOutputNotification = true
                }
            }
            .with { _, data ->
                val characteristicValue = data.value ?: return@with
                logMe("SliteOutputWriteSuccess") { String(characteristicValue) }
            }
            .fail { _, _ -> ignoreOutputNotification = false }
            .enqueue()
    }

    fun readSliteModeAndStatus() {
        readCharacteristic(sliteStatusControlPoint)
            .with { _, data ->
                val characteristicValue = data.value ?: return@with
                onSliteConfigurationModeChanged?.invoke(
                    LightConfigurationMode.fromSliteStatusCharacteristic(String(characteristicValue))
                )
            }
            .enqueue()
    }

    fun writeSliteConfigurationMode(configurationMode: LightConfigurationMode) {
        writeCharacteristic(
            sliteStatusControlPoint,
            Data.from(configurationMode.toConfigurationModeDeviceValue()),
            WRITE_TYPE_NO_RESPONSE
        )
            .with { _, data ->
                ignoreModeAndStatusNotification = true
                val characteristicValue = data.value ?: return@with
                logMe("SliteStatusWriteSuccess") { String(characteristicValue) }
            }
            .enqueue()
    }

    fun readSliteVersion() {
        readCharacteristic(sliteReadVersionControlPoint)
            .with { _, data ->
                val characteristicValue = data.value ?: return@with
                val sliteVersion = SliteVersion.deriveFromVersionCharacteristic(characteristicValue)

                logMe { "[Update] Slite version detected: " + sliteVersion.softwareVersion + " hw: " + sliteVersion.hardwareVersion }
                refreshUpdateState(sliteVersion.softwareVersion)
                onSliteUpdateStatusChanged?.invoke(updateState)
            }
            .enqueue()
    }

    private fun refreshUpdateState(softwareVersion: Version) {
        if (updateState is UpdateState.InProgress) return

        if (UpdateProvider.isUpdateAvailableForVersion(softwareVersion)) {
            updateUpdateState(UpdateState.Available(softwareVersion))
        } else {
            updateUpdateState(UpdateState.NotAvailable(softwareVersion))
        }
    }

    fun getUpdateState(): UpdateState {
        return updateState
    }

    fun startUpdateProcess() {
        if (updateState is UpdateState.InProgress || updateState is UpdateState.NotAvailable) return

        logMe { "[Update] Starting update process!" }
        updateUpdateState(UpdateState.InProgress(updateState.currentVersion, 0))

        updateProvider.startUpdateProcess(
            onChunkReadyForWriting = { chunkToWrite ->
                writeCharacteristic(sliteUpdateVersionControlPoint, chunkToWrite, WRITE_TYPE_DEFAULT)
                    .with { _, _ ->
                        updateProvider.onChunkWritten(
                            onUpdateProgressChanged = { progress ->
                                updateUpdateState(UpdateState.InProgress(updateState.currentVersion, progress))
                            }
                        )
                    }
                    .enqueue()
            },
            onAllChunksWritten = {
                updateUpdateState(UpdateState.Completed())
            }
        )
    }

    private fun updateUpdateState(newUpdateState: UpdateState) {
        updateState = newUpdateState
        onSliteUpdateStatusChanged?.invoke(updateState)
    }

    private inner class SliteGattCallback : BleManagerGattCallback() {

        override fun initialize() {
            setNotificationCallback(lightOutputControlPoint).with { _, data ->
                if (ignoreOutputNotification) {
                    ignoreOutputNotification = false
                    return@with
                }
                val characteristicValue = data.value ?: return@with
                logMe("WRKROutputNotification") { String(characteristicValue) }

                onSliteOutputChanged?.invoke(fromCharacteristicByteArray(characteristicValue))
            }

            setNotificationCallback(sliteStatusControlPoint).with { _, data ->
                if (ignoreModeAndStatusNotification) {
                    ignoreModeAndStatusNotification = false
                    return@with
                }
                val characteristicValue = data.value ?: return@with
                val responseString = String(characteristicValue)
                logMe("WRKRStatusNotification") { responseString }

                onSliteConfigurationModeChanged?.invoke(
                    LightConfigurationMode.fromSliteStatusCharacteristic(responseString)
                )
            }

            beginAtomicRequestQueue()
                .add(requestMtu(SLITE_MTU))
                .add(enableNotifications(lightOutputControlPoint))
                .add(enableNotifications(sliteStatusControlPoint))
                .enqueue()
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val sliteBluetoothService = gatt.getService(SLITE_SERVICE_ID) ?: return false
            lightOutputControlPoint = sliteBluetoothService.getCharacteristic(SLITE_LIGHT_OUTPUT_CHARACTERISTIC_ID)
            sliteStatusControlPoint = sliteBluetoothService.getCharacteristic(SLITE_STATUS_CHARACTERISTIC_ID)

            val sliteVersionService = gatt.getService(SLITE_UPDATE_SERVICE_ID) ?: return false
            sliteReadVersionControlPoint = sliteVersionService.getCharacteristic(SLITE_UPDATE_READ_CHARACTERISTIC_ID)
            sliteUpdateVersionControlPoint = sliteVersionService.getCharacteristic(SLITE_UPDATE_WRITE_CHARACTERISTIC_ID)

            return lightOutputControlPoint != null && sliteStatusControlPoint != null
                    && sliteReadVersionControlPoint != null && sliteUpdateVersionControlPoint != null
        }

        override fun onServicesInvalidated() {
            lightOutputControlPoint = null
            sliteStatusControlPoint = null
            sliteReadVersionControlPoint = null
            sliteUpdateVersionControlPoint = null
        }
    }

    fun setOnLightOutputChangedListener(onSliteOutputChanged: OnSliteOutputChanged) {
        this.onSliteOutputChanged = onSliteOutputChanged
    }

    fun setOnSliteConfigurationModeChangedListener(onSliteConfigurationModeChanged: OnSliteConfigurationModeChanged) {
        this.onSliteConfigurationModeChanged = onSliteConfigurationModeChanged
    }

    fun setOnSliteUpdateStatusChanged(onSliteUpdateStatusChanged: OnSliteUpdateStatusChanged) {
        this.onSliteUpdateStatusChanged = onSliteUpdateStatusChanged
    }

    companion object {
        private const val SLITE_MTU = 255
        private val SLITE_SERVICE_ID = UUID.fromString("9c193637-b20b-49d7-b9c5-8560c97328f5")
        private val SLITE_LIGHT_OUTPUT_CHARACTERISTIC_ID = UUID.fromString("f0c93649-05fc-45fa-b0c8-d80b83ee00f6")
        private val SLITE_STATUS_CHARACTERISTIC_ID = UUID.fromString("f0c93649-05fc-45fa-b0c8-d80b83ee00f7")

        private val SLITE_UPDATE_SERVICE_ID = UUID.fromString("C8659210-AF91-4AD3-A995-A58D6FD26145")
        private val SLITE_UPDATE_READ_CHARACTERISTIC_ID = UUID.fromString("C8659212-AF91-4AD3-A995-A58D6FD26145")
        private val SLITE_UPDATE_WRITE_CHARACTERISTIC_ID = UUID.fromString("C8659211-AF91-4AD3-A995-A58D6FD26145")

        private const val COLOR_MODE_SLITE_VALUE = "HUE"
        private const val SATURATION_MODE_SLITE_VALUE = "SATURATION"
        private const val WHITE_MODE_SLITE_VALUE = "COLOURTEMP"
        private const val GM_ADJUST_MODE_SLITE_VALUE = "WHITE_GM_ADJUST"
        private const val EFFECT_MODE_SLITE_VALUE = "EFFECT_STREAMING"

        val colorConfigurationModeSliteValues = listOf(COLOR_MODE_SLITE_VALUE, SATURATION_MODE_SLITE_VALUE)
        val whiteConfigurationModeSliteValues = listOf(WHITE_MODE_SLITE_VALUE, GM_ADJUST_MODE_SLITE_VALUE)

        private fun LightConfigurationMode.toConfigurationModeDeviceValue(): String =
            when (this) {
                WHITE -> WHITE_MODE_SLITE_VALUE
                COLORS -> COLOR_MODE_SLITE_VALUE
                EFFECTS -> EFFECT_MODE_SLITE_VALUE
            }
    }
}