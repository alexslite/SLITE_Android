package com.sliteptyltd.slite.data.model.version;

data class Version(val major: Int, val minor: Int, val hotfix: Int = 0) : Comparable<Version> {

    override fun compareTo(other: Version): Int {
        val majorVersion = major.compareTo(other.major)
        if (majorVersion != DEFAULT_VERSION_NUMBER) return majorVersion

        val minorVersion = minor.compareTo(other.minor)
        if (minorVersion != DEFAULT_VERSION_NUMBER) return minorVersion

        return hotfix.compareTo(other.hotfix)
    }

    override fun toString(): String {
        return "$major.$minor.$hotfix"
    }

    companion object {
        val ZERO = Version(0, 0, 0)

        private const val DEFAULT_VERSION_NUMBER = 0
        private const val VERSION_DELIMITER = "."
        private const val VERSION_FULL_EXPECTED_LENGTH = 3
        private const val VERSION_PARTIAL_EXPECTED_LENGTH = 2

        fun parseFromString(version: String): Version {
            val split = version.split(VERSION_DELIMITER)

            return when (split.size) {
                VERSION_FULL_EXPECTED_LENGTH -> Version(
                    split[0].toIntOrNull() ?: DEFAULT_VERSION_NUMBER,
                    split[1].toIntOrNull() ?: DEFAULT_VERSION_NUMBER,
                    split[2].toIntOrNull() ?: DEFAULT_VERSION_NUMBER
                )

                VERSION_PARTIAL_EXPECTED_LENGTH -> Version(
                    split[0].toIntOrNull() ?: DEFAULT_VERSION_NUMBER,
                    split[1].toIntOrNull() ?: DEFAULT_VERSION_NUMBER
                )

                else -> ZERO
            }
        }
    }
}
