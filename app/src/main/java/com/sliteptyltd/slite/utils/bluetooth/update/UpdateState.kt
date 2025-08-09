package com.sliteptyltd.slite.utils.bluetooth.update

import com.sliteptyltd.slite.data.model.version.Version

sealed class UpdateState(val currentVersion: Version) {
    class NotAvailable(version: Version) : UpdateState(version)
    class Available(version: Version) : UpdateState(version)
    class InProgress(version: Version, val progress: Int) : UpdateState(version)
    class Interrupted(version: Version) : UpdateState(version)
    class Completed : UpdateState(UpdateProvider.LATEST_AVAILABLE_SOFTWARE_VERSION)
}
