package com.sliteptyltd.slite.data.model.version

data class SliteVersion(
    val hardwareVersion: Version,
    val softwareVersion: Version
) {
    companion object {
        private const val HW_VERSION_FIRST_BYTE_INDEX = 0
        private const val HW_VERSION_SECOND_BYTE_INDEX = 1
        private const val SW_VERSION_FIRST_BYTE_INDEX = 2
        private const val SW_VERSION_SECOND_BYTE_INDEX = 3
        private const val SW_VERSION_THIRD_BYTE_INDEX = 4

        private val ZERO = SliteVersion(Version.ZERO, Version.ZERO)

        fun deriveFromVersionCharacteristic(characteristicValue: ByteArray): SliteVersion {
            if (characteristicValue.size != 5) {
                return ZERO
            }

            val hardwareVersion =
                Version.parseFromString("${characteristicValue[HW_VERSION_FIRST_BYTE_INDEX]}.${characteristicValue[HW_VERSION_SECOND_BYTE_INDEX]}")
            val softwareVersion =
                Version.parseFromString("${characteristicValue[SW_VERSION_FIRST_BYTE_INDEX]}.${characteristicValue[SW_VERSION_SECOND_BYTE_INDEX]}.${characteristicValue[SW_VERSION_THIRD_BYTE_INDEX]}")

            return SliteVersion(hardwareVersion, softwareVersion)
        }
    }
}