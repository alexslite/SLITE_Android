package com.sliteptyltd.slite.utils.handlers

import com.sliteptyltd.slite.BuildConfig.VERSION_NAME

class InAppUpdateHandler {

//    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(context) }
//
//    fun checkIfUpdateIsAvailable(activity: Activity, updatePriority: UpdatePriority) {
//        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
//            if (appUpdateInfo.updateAvailability() != UPDATE_AVAILABLE) return@addOnSuccessListener
//
//            when (updatePriority) {
//                UpdatePriority.MANDATORY -> startFlexibleUpdate(appUpdateInfo, activity)
//                UpdatePriority.RECOMMENDED -> startImmediateUpdate(appUpdateInfo, activity)
//                UpdatePriority.NOT_NECESSARY -> Unit
//                UpdatePriority.NOT_NEEDED -> Unit
//            }
//        }
//    }
//
//    fun completeUpdate() = appUpdateManager.completeUpdate()
//
//    fun registerInstallStateUpdateListener(installStateUpdatedListener: InstallStateUpdatedListener) {
//        appUpdateManager.registerListener(installStateUpdatedListener)
//    }
//
//    fun unregisterInstallStateUpdateListener(installStateUpdatedListener: InstallStateUpdatedListener) {
//        appUpdateManager.unregisterListener(installStateUpdatedListener)
//    }
//
//    private fun startFlexibleUpdate(appUpdateInfo: AppUpdateInfo, activity: Activity) {
//        appUpdateManager.startUpdateFlowForResult(
//            appUpdateInfo,
//            FLEXIBLE,
//            activity,
//            IN_APP_UPDATE_REQUEST_CODE
//        )
//    }
//
//    private fun startImmediateUpdate(appUpdateInfo: AppUpdateInfo, activity: Activity) {
//        appUpdateManager.startUpdateFlowForResult(
//            appUpdateInfo,
//            IMMEDIATE,
//            activity,
//            IN_APP_UPDATE_REQUEST_CODE
//        )
//    }

    fun determineUpdatePriority(storeAppVersion: String): UpdatePriority {
        val installedAppVersionNumbers = VERSION_NAME.split(VERSION_CODE_PRIORITY_DELIMITER)
        val storeAppVersionNumbers = storeAppVersion.split(VERSION_CODE_PRIORITY_DELIMITER)

        val installedMajorReleaseNumber = installedAppVersionNumbers[MAJOR_RELEASE_NUMBER_INDEX].toInt()
        val storeMajorReleaseNumber = storeAppVersionNumbers[MAJOR_RELEASE_NUMBER_INDEX].toInt()

        val installedMinorReleaseNumber = installedAppVersionNumbers[MINOR_RELEASE_NUMBER_INDEX].toInt()
        val storeMinorReleaseNumber = storeAppVersionNumbers[MINOR_RELEASE_NUMBER_INDEX].toInt()

        val installedFixesReleaseNumber = installedAppVersionNumbers[FIXES_RELEASE_NUMBER_INDEX].toInt()
        val storeFixesReleaseNumber = storeAppVersionNumbers[FIXES_RELEASE_NUMBER_INDEX].toInt()

        return when {
            storeMajorReleaseNumber > installedMajorReleaseNumber -> UpdatePriority.MANDATORY
            storeMinorReleaseNumber > installedMinorReleaseNumber -> UpdatePriority.RECOMMENDED
            storeFixesReleaseNumber > installedFixesReleaseNumber -> UpdatePriority.NOT_NECESSARY
            else -> UpdatePriority.NOT_NEEDED
        }
    }

    enum class UpdatePriority {
        MANDATORY, RECOMMENDED, NOT_NECESSARY, NOT_NEEDED;

        val requiresUpdatePrompt: Boolean get() = this == MANDATORY || this == RECOMMENDED
    }

    companion object {
//        const val IN_APP_UPDATE_REQUEST_CODE = 5001
        private const val MAJOR_RELEASE_NUMBER_INDEX = 0
        private const val MINOR_RELEASE_NUMBER_INDEX = 1
        private const val FIXES_RELEASE_NUMBER_INDEX = 2
        private const val VERSION_CODE_PRIORITY_DELIMITER =  "."
    }
}