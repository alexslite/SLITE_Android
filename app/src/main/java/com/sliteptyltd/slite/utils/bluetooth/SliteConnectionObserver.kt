package com.sliteptyltd.slite.utils.bluetooth

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.ble.observer.ConnectionObserver

open class SliteConnectionObserver : ConnectionObserver {

    override fun onDeviceConnecting(device: BluetoothDevice) = Unit

    override fun onDeviceConnected(device: BluetoothDevice) = Unit

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) = Unit

    override fun onDeviceReady(device: BluetoothDevice) = Unit

    override fun onDeviceDisconnecting(device: BluetoothDevice) = Unit

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) = Unit
}