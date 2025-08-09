package com.sliteptyltd.slite.utils.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_POWER
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.os.ParcelUuid
import com.sliteptyltd.slite.data.model.light.LightConfigurationMode
import com.sliteptyltd.slite.data.model.version.Version
import com.sliteptyltd.slite.utils.Constants.Connectivity.SLITE_NAME_FILTERING_PREFIX
import com.sliteptyltd.slite.utils.bluetooth.update.UpdateState

typealias OnScanSuccessListener = (sliteDevice: BluetoothDevice) -> Unit

@SuppressLint("MissingPermission")
class BluetoothService(
    private val context: Context
) {

    private val bluetoothAdapter: BluetoothAdapter by lazy { initBluetoothAdapter(context) }
    private val bleScanner by lazy { bluetoothAdapter.bluetoothLeScanner }
    private var onScanSuccessListener: OnScanSuccessListener? = null
    private val scanCallback by lazy { initScanCallback() }
    private val sliteBleManagers = mutableMapOf<String, SliteBleManager>()
    private val devicesSubscribers = mutableMapOf<String, MutableList<SliteEventSubscriber>>()

    private var updateAvailableForAtLeastOneSubscriber = false

    val isBluetoothEnabled: Boolean get() = bluetoothAdapter.isEnabled
    val isScanning: Boolean get() = bluetoothAdapter.isDiscovering
    val isUpdateAvailableForAtLeastOneDevice: Boolean get() = updateAvailableForAtLeastOneSubscriber

    fun startScan() {
        val scanSettings = ScanSettings.Builder().setScanMode(SCAN_MODE_LOW_POWER).build()

        val scanFilters = ScanFilter.Builder().setServiceUuid(SLITE_SERVICE_ID).build()

        bleScanner.startScan(listOf(scanFilters), scanSettings, scanCallback)
    }

    fun stopScan() {
        bleScanner.stopScan(scanCallback)
    }

    fun disconnectAll() {
        sliteBleManagers.forEach { (_, bluetoothGatt) ->
            bluetoothGatt.disconnect().enqueue()
            bluetoothGatt.close()
        }
        sliteBleManagers.clear()
    }

    fun disconnect(address: String) {
        sliteBleManagers[address]?.disconnect()?.enqueue()
        devicesSubscribers[address]?.clear()
    }

    fun connect(address: String) {
        if (sliteBleManagers[address] != null) {
            sliteBleManagers[address]?.readLightOutput()
            sliteBleManagers[address]?.readSliteModeAndStatus()
            sliteBleManagers[address]?.readSliteVersion()
            return
        }

        bluetoothAdapter.getRemoteDevice(address)?.let { sliteDevice ->
            val sliteBleManager = SliteBleManager(context)
            sliteBleManager
                .connect(sliteDevice)
                .timeout(SLITE_CONNECTION_REQUEST_TIMEOUT)
                .done {
                    onSliteConnected(sliteBleManager)
                }.fail { _, _ ->
                    notifySliteConnectionStateChanged(address, false)
                }.enqueue()
        }
    }

    private fun onSliteConnected(sliteBleManager: SliteBleManager) {
        val sliteAddress = sliteBleManager.bluetoothDevice?.address ?: return

        if (sliteBleManagers[sliteAddress] == null) {
            sliteBleManagers[sliteAddress] = sliteBleManager

            sliteBleManager.setOnLightOutputChangedListener { lightCharacteristic ->
                notifySliteOutputValueChanged(sliteAddress, lightCharacteristic)
            }

            sliteBleManager.setOnSliteConfigurationModeChangedListener { configurationMode ->
                notifySliteConfigurationModeChanged(sliteAddress, configurationMode)
            }

            sliteBleManager.setOnSliteUpdateStatusChanged { updateState ->
                notifySliteUpdateStateChanged(sliteAddress, updateState)
                recheckUpdateAvailability()
            }

            sliteBleManager.connectionObserver = initSliteConnectionObserver()

            notifySliteConnectionStateChanged(sliteAddress, true)
        }

        sliteBleManager.readLightOutput()
        sliteBleManager.readSliteModeAndStatus()
        sliteBleManager.readSliteVersion()
    }

    fun setSliteOutputValue(
        address: String,
        sliteLightCharacteristic: SliteLightCharacteristic,
        ignoreBlackout: Boolean = false,
        ignoreWriteOutputNotification: Boolean = true
    ) {
        sliteBleManagers[address]?.writeLightOutput(
            sliteLightCharacteristic, ignoreBlackout, ignoreWriteOutputNotification
        )
    }

    fun readSliteOutputValue(address: String) {
        sliteBleManagers[address]?.readLightOutput()
    }

    fun readSliteModeAndStatus(address: String) {
        sliteBleManagers[address]?.readSliteModeAndStatus()
    }

    fun readSliteVersion(address: String) {
        sliteBleManagers[address]?.readSliteVersion()
    }

    private fun notifySliteOutputValueChanged(address: String, lightCharacteristic: SliteLightCharacteristic) {
        devicesSubscribers[address]?.forEach { sliteEventSubscriber: SliteEventSubscriber ->
            sliteEventSubscriber.onLightCharacteristicChanged(address, lightCharacteristic)
        }
    }

    fun setSliteConfigurationMode(
        address: String, configurationMode: LightConfigurationMode
    ) {
        sliteBleManagers[address]?.writeSliteConfigurationMode(configurationMode)
    }

    private fun notifySliteConfigurationModeChanged(address: String, configurationMode: LightConfigurationMode) {
        devicesSubscribers[address]?.forEach { sliteEventSubscriber: SliteEventSubscriber ->
            sliteEventSubscriber.onLightConfigurationModeChanged(address, configurationMode)
        }
    }

    private fun notifySliteConnectionStateChanged(address: String, isConnected: Boolean) {
        devicesSubscribers[address]?.forEach { sliteEventSubscriber: SliteEventSubscriber ->
            sliteEventSubscriber.onConnectionStateChanged(address, isConnected)
        }
    }

    private fun notifySliteUpdateStateChanged(address: String, updateState: UpdateState) {
        devicesSubscribers[address]?.forEach { sliteEventSubscriber: SliteEventSubscriber ->
            sliteEventSubscriber.onUpdateStateChanged(address, updateState)
        }
    }

    private fun recheckUpdateAvailability() {
        updateAvailableForAtLeastOneSubscriber = false

        sliteBleManagers.forEach { (_, slite) ->
            if (slite.getUpdateState() is UpdateState.Available || slite.getUpdateState() is UpdateState.InProgress) {
                updateAvailableForAtLeastOneSubscriber = true
                return
            }
        }
    }

    fun getUpdateStateForSubscriber(address: String): UpdateState {
        val sliteManager = sliteBleManagers[address]
        return sliteManager?.getUpdateState() ?: UpdateState.NotAvailable(Version.ZERO)
    }

    fun startUpdateForSubscriber(address: String) {
        val sliteManager = sliteBleManagers[address] ?: return
        sliteManager.startUpdateProcess()
    }

    fun isSubscriberConnected(address: String): Boolean {
        return sliteBleManagers[address]?.isConnected ?: false
    }

    fun addDeviceSubscriber(address: String, deviceSubscriber: SliteEventSubscriber) {
        val subscribersList = devicesSubscribers[address]
        if (subscribersList != null) {
            subscribersList.add(deviceSubscriber)
        } else {
            devicesSubscribers[address] = mutableListOf(deviceSubscriber)
        }
    }

    fun removeDeviceSubscriber(address: String, deviceSubscriber: SliteEventSubscriber) {
        devicesSubscribers[address]?.remove(deviceSubscriber)
    }

    fun setOnScanSuccessListener(onScanSuccessListener: OnScanSuccessListener) {
        this.onScanSuccessListener = onScanSuccessListener
    }

    private fun initSliteConnectionObserver(): SliteConnectionObserver = object : SliteConnectionObserver() {

        override fun onDeviceReady(device: BluetoothDevice) {
            notifySliteConnectionStateChanged(device.address, true)
        }

        override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
            sliteBleManagers[device.address]?.close()
            sliteBleManagers.remove(device.address)
            recheckUpdateAvailability()
            notifySliteConnectionStateChanged(device.address, false)
        }
    }

    private fun initBluetoothAdapter(context: Context): BluetoothAdapter =
        (context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter

    private fun initScanCallback(): ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            if (result?.device?.name?.contains(SLITE_NAME_FILTERING_PREFIX) == true) {
                onScanSuccessListener?.invoke(result.device)
            }
        }
    }

    companion object {
        private val SLITE_SERVICE_ID = ParcelUuid.fromString("00001811-0000-1000-8000-00805f9b34fb")
        private const val SLITE_CONNECTION_REQUEST_TIMEOUT = 5000L
    }
}