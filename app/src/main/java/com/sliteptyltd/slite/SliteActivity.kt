package com.sliteptyltd.slite

import SliteActivityViewModel
import android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED
import android.bluetooth.BluetoothAdapter.EXTRA_STATE
import android.bluetooth.BluetoothAdapter.STATE_OFF
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.CONSUMED
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.sliteptyltd.slite.analytics.AnalyticsService
import com.sliteptyltd.slite.data.preference.InternalStorageManager
import com.sliteptyltd.slite.databinding.ActivitySliteBinding
import com.sliteptyltd.slite.feature.effectsservice.EffectsHandler
import com.sliteptyltd.slite.feature.missingcapabilities.MissingCapabilitiesFragment.Companion.navigateToMissingCapabilitiesFragment
import com.sliteptyltd.slite.feature.missingcapabilities.MissingCapability.BLUETOOTH_CONNECTIVITY
import com.sliteptyltd.slite.feature.missingcapabilities.MissingCapability.BLUETOOTH_PERMISSIONS
import com.sliteptyltd.slite.utils.bluetooth.BluetoothService
import com.sliteptyltd.slite.utils.extensions.hasBluetoothCapabilitiesPermissions
import com.sliteptyltd.slite.utils.extensions.matchDestination
import com.sliteptyltd.slite.utils.extensions.viewBinding
import com.sliteptyltd.slite.utils.handlers.DialogHandler
import com.sliteptyltd.slite.utils.handlers.InAppUpdateHandler
import com.sliteptyltd.slite.utils.handlers.InAppUpdateHandler.UpdatePriority.MANDATORY
import com.sliteptyltd.slite.utils.handlers.InAppUpdateHandler.UpdatePriority.RECOMMENDED
import com.sliteptyltd.slite.views.indicatornavigation.BottomNavigationTab.LIGHTS
import com.sliteptyltd.slite.views.indicatornavigation.BottomNavigationTab.SCENES
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SliteActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivitySliteBinding::inflate)
    private val viewModel by viewModel<SliteActivityViewModel>()
    private val navController: NavController by lazy { initNavController() }
    private val bluetoothService by inject<BluetoothService>()
    private val effectsHandler by inject<EffectsHandler>()
    private val internalStorageManager by inject<InternalStorageManager>()
    private val inAppUpdateHandler by inject<InAppUpdateHandler>()
    private val dialogHandler by inject<DialogHandler>()
    private val analyticsService by inject<AnalyticsService>()
    private val bluetoothStateBroadcastReceiver by lazy { initBluetoothStateBroadcastReceiver() }
    //    private val announcementHandler by inject<AnnouncementHandler>()
//    private val installStateUpdatedListener by lazy { initInstallStateUpdateListener() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        analyticsService.initAnalyticsService(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        handleSystemBarsOverlaps()
        if (savedInstanceState != null && !hasBluetoothCapabilitiesPermissions) {
            navController.navigateToMissingCapabilitiesFragment(BLUETOOTH_PERMISSIONS)
        }
        checkLatestAppVersion()

        setupNavController()
        setupBottomNavigation()
        addOnBackPressedCallback()
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(bluetoothStateBroadcastReceiver, IntentFilter(ACTION_STATE_CHANGED))
    }

    override fun onStop() {
        unregisterReceiver(bluetoothStateBroadcastReceiver)
//        inAppUpdateHandler.unregisterInstallStateUpdateListener(installStateUpdatedListener)
        viewModel.storeLatestLightsAndClearEffectsSettings()
        super.onStop()
    }

    override fun onDestroy() {
        bluetoothService.disconnectAll()
        super.onDestroy()
    }

//    @Suppress("DEPRECATION", "DeprecatedCallableAddReplaceWith")
//    @Deprecated("Deprecated in Java")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode != IN_APP_UPDATE_REQUEST_CODE) return
//
//        when (resultCode) {
//            RESULT_IN_APP_UPDATE_FAILED -> announcementHandler.showWarningAnnouncement(this, getString(R.string.app_update_failed_message))
//            RESULT_CANCELED -> announcementHandler.showWarningAnnouncement(this, getString(R.string.app_update_cancelled_message))
//        }
//    }

    private fun checkLatestAppVersion() {
        Firebase.remoteConfig.fetchAndActivate().addOnSuccessListener(this) {
            val latestStoreAppVersion = Firebase.remoteConfig.getString(FIREBASE_REMOTE_CONFIG_APP_VERSION_KEY)
            val updatePriority =
                inAppUpdateHandler.determineUpdatePriority(latestStoreAppVersion)

            if (updatePriority == MANDATORY || (updatePriority == RECOMMENDED && latestStoreAppVersion != internalStorageManager.latestRecommendedAppVersionShown)) {
                dialogHandler.showAppUpdateNoticeDialog(this, updatePriority == MANDATORY) {
                    internalStorageManager.latestRecommendedAppVersionShown = latestStoreAppVersion
                }
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.mainBottomNavigation.setOnTabSelectedListener { tab ->
            when (tab) {
                LIGHTS -> navigateToLightsGraph()
                SCENES -> navigateToScenesGraph()
            }
        }
    }

    private fun setupNavController() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.mainBottomNavigation.isVisible = destination.id in BOTTOM_NAVIGATION_FRAGMENTS
            when {
                destination.matchDestination(R.id.lightsFragment) -> binding.mainBottomNavigation.selectTab(LIGHTS)
                destination.matchDestination(R.id.scenesFragment) -> binding.mainBottomNavigation.selectTab(SCENES)
            }
        }
    }

    private fun addOnBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when (navController.currentDestination?.id) {
                    R.id.scenesFragment -> navController.navigate(HomeGraphDirections.toLightsGraph())
                    R.id.lightsFragment -> finish()
                    else -> onSupportNavigateUp()
                }
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean = navController.navigateUp()

    private fun navigateToLightsGraph() {
        if (binding.mainBottomNavigation.selectedTab != LIGHTS) {
            navController.navigate(HomeGraphDirections.toLightsGraph())
        }
    }

    private fun navigateToScenesGraph() {
        if (binding.mainBottomNavigation.selectedTab != SCENES) {
            navController.navigate(HomeGraphDirections.toScenesGraph())
        }
    }

    private fun handleSystemBarsOverlaps() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainBottomNavigation) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.mainBottomNavigation.updatePadding(bottom = insets.bottom)
            CONSUMED
        }
    }

    private fun initNavController(): NavController =
        (supportFragmentManager.findFragmentById(R.id.sliteNavHostFragment) as NavHostFragment).navController

    private fun initBluetoothStateBroadcastReceiver() = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (!internalStorageManager.hasUserAddedFirstDevice) return
            val action = intent?.action ?: return
            if (action != ACTION_STATE_CHANGED) return
            if (intent.isBluetoothOff) {
                effectsHandler.stopAllEffects()
                navController.navigateToMissingCapabilitiesFragment(BLUETOOTH_CONNECTIVITY)
            } else if (navController.currentDestination?.id == R.id.missingCapabilitiesFragment && hasBluetoothCapabilitiesPermissions) {
                navController.navigateUp()
            }
        }
    }

//    private fun initInstallStateUpdateListener(): InstallStateUpdatedListener =
//        InstallStateUpdatedListener { installState ->
//            if (installState.installStatus() == DOWNLOADED) {
//                Snackbar.make(
//                    binding.root,
//                    getString(R.string.app_update_downloaded_informational_text),
//                    Snackbar.LENGTH_INDEFINITE
//                ).apply {
//                    setAction(getString(R.string.app_update_downloaded_apply_update_button_text)) { inAppUpdateHandler.completeUpdate() }
//                    setActionTextColor(resources.getColor(R.color.text_color, null))
//                    show()
//                }
//            }
//        }

    companion object {
        private const val FIREBASE_REMOTE_CONFIG_APP_VERSION_KEY = "androidCurrentVersion"
        private val BOTTOM_NAVIGATION_FRAGMENTS = listOf(R.id.lightsFragment, R.id.scenesFragment, R.id.lightConfigurationDialogFragment)
        private val Intent?.isBluetoothOff get() = this?.getIntExtra(EXTRA_STATE, -1) == STATE_OFF
    }
}