package com.sliteptyltd.slite.feature.lightconfiguration.imagecolor

import android.Manifest.permission.*
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Color.WHITE
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.AccelerateInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.CONSUMED
import androidx.core.view.isInvisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.analytics.AnalyticsService
import com.sliteptyltd.slite.databinding.FragmentImageColorPickerBinding
import com.sliteptyltd.slite.utils.Constants.ColorUtils.MAX_RGB_COMPONENT_VALUE
import com.sliteptyltd.slite.utils.Constants.Connectivity.LIGHT_CONFIGURATION_UPDATE_THROTTLE
import com.sliteptyltd.slite.utils.Constants.Lights.DEFAULT_RESIZE_ANIMATION_DURATION
import com.sliteptyltd.slite.utils.Constants.Lights.SLITE_STATUS_BLACKOUT
import com.sliteptyltd.slite.utils.Constants.Lights.SLITE_STATUS_ON
import com.sliteptyltd.slite.utils.bluetooth.BluetoothService
import com.sliteptyltd.slite.utils.bluetooth.SliteEventSubscriber
import com.sliteptyltd.slite.utils.bluetooth.SliteLightCharacteristic
import com.sliteptyltd.slite.utils.color.colorHue
import com.sliteptyltd.slite.utils.color.colorSaturation
import com.sliteptyltd.slite.utils.color.colorWithMaxBrightness
import com.sliteptyltd.slite.utils.extensions.getCenterPixelColor
import com.sliteptyltd.slite.utils.extensions.isResumed
import com.sliteptyltd.slite.utils.extensions.openAppSettings
import com.sliteptyltd.slite.utils.handlers.DialogHandler
import com.sliteptyltd.slite.utils.logMe
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

class ImageColorPickerFragment : Fragment(R.layout.fragment_image_color_picker) {

    private val binding by viewBinding(FragmentImageColorPickerBinding::bind)
    private val args by navArgs<ImageColorPickerFragmentArgs>()
    private val viewModel by viewModel<ImageColorPickerViewModel> { parametersOf(args.lightConfigurationDetails) }
    private val dialogHandler by inject<DialogHandler>()
    private val bluetoothService by inject<BluetoothService>()
    private val analyticsService by inject<AnalyticsService>()
    private val selectedLightId: Int by lazy { args.lightConfigurationDetails.id }
    private val lightEventsSubscriber by lazy { initSliteEventSubscriber() }
    private var selectedColorInt: Int = DEFAULT_PREVIEW_COLOR
    private val cameraExecutor: ExecutorService by lazy { initCameraExecutor() }
    private val hasCameraPermission: Boolean get() = getHasCameraPermissions()
    private val hasReadStoragePermission: Boolean get() = getHasReadStoragePermissions()
    private val requestCameraPermissionLauncher = initRequestCameraPermissionLauncher()
    private val requestReadStoragePermissionLauncher = initRequestReadStoragePermissionLauncher()
    private val getImageFromGalleryRequestLauncher = initGetImageFromGalleryRequestLauncher()
    private var cameraProvider: ProcessCameraProvider? = null
    private val onCrossHairDragListener by lazy { initCrosshairDragListener() }
    private val onSelectedImageTouchListener by lazy { initSelectedImageTouchListener() }
    private val colorPreviewResizeAnimator by lazy { initColorPreviewResizeAnimator() }
    private var colorIndicatorContainerInitialX = 0f
    private var colorIndicatorContainerInitialY = 0f

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleSystemBarsOverlaps()
        setUIStateForColorPickerMode(viewModel.isInCameraMode)
        storeColorIndicatorContainerInitialCoordinates()
        setupOnBackPressedCallback()
        setupListeners()
        updateColorPickerButtons(false)

        val sliteAddress = args.lightConfigurationDetails.address ?: return
        bluetoothService.addDeviceSubscriber(sliteAddress, lightEventsSubscriber)
    }

    override fun onResume() {
        super.onResume()
        if (!viewModel.shouldCheckPermissions) return

        if (viewModel.isInCameraMode) {
            handleCameraModePermissions()
        } else {
            handleGalleryModePermissions()
        }
    }

    private fun handleCameraModePermissions() {
        if (!hasCameraPermission) {
            requestCameraPermissions()
        } else {
            setIsTargetedColorLockedIn(false)
            viewModel.isInCameraMode = true
            binding.confirmColorBtn.isEnabled = true
            setUIStateForColorPickerMode(true)
            startCamera()
        }
    }

    private fun handleGalleryModePermissions() {
        if (!hasReadStoragePermission) {
            requestGalleryPermissions()
        } else {
            viewModel.shouldCheckPermissions = false
            getImageFromGalleryRequestLauncher.launch(STORAGE_MEDIA_TYPE_REQUEST)
        }
    }

    override fun onDestroyView() {
        cameraExecutor.shutdown()
        cameraProvider = null
        val sliteAddress = args.lightConfigurationDetails.address
        if (sliteAddress != null) {
            bluetoothService.removeDeviceSubscriber(sliteAddress, lightEventsSubscriber)
        }
        super.onDestroyView()
    }

    private fun startCamera() {
        binding.selectedImageIV.setImageURI(null)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            cameraProvider?.let { cameraProvider -> showTargetedPixelColor(cameraProvider) }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun showTargetedPixelColor(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(binding.cameraPreview.surfaceProvider)

        try {
            cameraProvider.unbindAll()

            val imageAnalysisUseCase = ImageAnalysis.Builder()
                .setOutputImageFormat(OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()

            imageAnalysisUseCase.setAnalyzer(cameraExecutor) { imageProxy ->
                if (!viewModel.isTargetedColorLockedIn.get()) {
                    val centerPixelBaseColor = imageProxy.getCenterPixelColor()
                    selectedColorInt = centerPixelBaseColor.colorWithMaxBrightness
                    binding.colorPreviewIV.imageTintList = ColorStateList.valueOf(centerPixelBaseColor.colorWithMaxBrightness)
                }
                imageProxy.close()
            }

            cameraProvider.bindToLifecycle(viewLifecycleOwner, DEFAULT_BACK_CAMERA, preview, imageAnalysisUseCase)
        } catch (e: Exception) {
            logMe("SliteCameraColorPickerFragment") { "ImageAnalysis UseCase binding failed $e" }
        }
    }

    private fun initCrosshairDragListener(): TouchDragListener =
        TouchDragListener(
            { dx, dy, _ -> onMoveCrosshair(dx, dy) },
            { onCrosshairTouchActionDown() },
            { onCrosshairTouchActionUp() }
        )

    private fun onMoveCrosshair(dx: Float, dy: Float) {
        binding.targetedColorIndicatorContainerLL.x += dx
        binding.targetedColorIndicatorContainerLL.y += dy

        setCrosshairTargetedPreviewColor()
    }

    private fun initSelectedImageTouchListener(): TouchDragListener =
        TouchDragListener(
            onActionMoveListener = { dx, dy, event ->
                event ?: return@TouchDragListener
                onSelectedImageTouch(event)

                if ((dx > MIN_MOVE_DISTANCE_FOR_ANIMATION_START_THRESHOLD || dy > MIN_MOVE_DISTANCE_FOR_ANIMATION_START_THRESHOLD)
                    && !colorPreviewResizeAnimator.isStarted
                ) {
                    onCrosshairTouchActionDown()
                }
            },
            onActionDownListener = { event ->
                event ?: return@TouchDragListener
                onSelectedImageTouch(event)
            },
            onActionUpListener = { onCrosshairTouchActionUp() }
        )

    private fun onSelectedImageTouch(event: MotionEvent) {
        binding.targetedColorIndicatorContainerLL.x =
            event.x - binding.pixelCrossHairIV.x - binding.pixelCrossHairIV.layoutParams.width
        binding.targetedColorIndicatorContainerLL.y =
            event.y - binding.pixelCrossHairIV.y - binding.pixelCrossHairIV.layoutParams.width

        setCrosshairTargetedPreviewColor()
    }

    private fun onCrosshairTouchActionUp() {
        animateColorPreviewResize(
            resources.getDimension(R.dimen.image_color_picker_color_preview_size).roundToInt()
        )
    }

    private fun onCrosshairTouchActionDown() {
        animateColorPreviewResize(
            resources.getDimension(R.dimen.image_color_picker_color_preview_size_large).roundToInt()
        )
    }

    private fun initColorPreviewResizeAnimator(): ValueAnimator =
        ValueAnimator().apply {
            duration = DEFAULT_RESIZE_ANIMATION_DURATION
            interpolator = AccelerateInterpolator()

            addUpdateListener {
                binding.colorPreviewIV.updateLayoutParams {
                    height = it.animatedValue as Int
                    width = it.animatedValue as Int
                }
            }
        }

    private fun animateColorPreviewResize(endSize: Int) {
        val currentSize = binding.colorPreviewIV.layoutParams.width
        if (currentSize == endSize) return
        colorPreviewResizeAnimator.apply {
            setIntValues(currentSize, endSize)
            start()
        }
    }

    private fun initSliteEventSubscriber(): SliteEventSubscriber = object : SliteEventSubscriber() {

        override fun onConnectionStateChanged(address: String, isConnected: Boolean) {
            if (!isConnected && viewLifecycleOwner.lifecycle.isResumed) {
                viewLifecycleOwner.lifecycleScope.launch {
                    navigateToLightConfigurationFragment()
                }
            }
        }

        override fun onLightCharacteristicChanged(address: String, lightCharacteristic: SliteLightCharacteristic) {
            val sliteAddress = args.lightConfigurationDetails.address ?: return

            if (lightCharacteristic.blackout == SLITE_STATUS_BLACKOUT || binding.confirmColorBtn.isLoading) {
                bluetoothService.removeDeviceSubscriber(sliteAddress, this)
                if (viewLifecycleOwner.lifecycle.isResumed) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        navigateToLightConfigurationFragment()
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.confirmColorBtn.setButtonClickListener {
            if (viewModel.isTargetedColorLockedIn.get() || !viewModel.isInCameraMode) {
                binding.confirmColorBtn.setIsLoading(true)
                viewModel.updateLightColor(selectedColorInt)
                trackUseSelectedColorEvent(selectedColorInt)
                if (args.lightConfigurationDetails.configuration.isIndividualLight) {
                    val sliteAddress = args.lightConfigurationDetails.address ?: return@setButtonClickListener
                    bluetoothService.setSliteOutputValue(
                        sliteAddress,
                        SliteLightCharacteristic(
                            args.lightConfigurationDetails.configuration.temperature,
                            selectedColorInt.colorHue,
                            selectedColorInt.colorSaturation.toFloat(),
                            args.lightConfigurationDetails.configuration.brightness.toFloat(),
                            SLITE_STATUS_ON
                        ),
                        ignoreWriteOutputNotification = false
                    )
                } else {
                    args.lightConfigurationDetails.configuration.lightsDetails.forEach { light ->
                        val sliteAddress = light.address ?: return@forEach
                        bluetoothService.setSliteOutputValue(
                            sliteAddress,
                            SliteLightCharacteristic(
                                args.lightConfigurationDetails.configuration.temperature,
                                selectedColorInt.colorHue,
                                selectedColorInt.colorSaturation.toFloat(),
                                args.lightConfigurationDetails.configuration.brightness.toFloat(),
                                SLITE_STATUS_ON
                            )
                        )
                    }
                    if (viewLifecycleOwner.lifecycle.isResumed) {
                        viewLifecycleOwner.lifecycleScope.launch {
                            delay(LIGHT_CONFIGURATION_UPDATE_THROTTLE)
                            navigateToLightConfigurationFragment()
                        }
                    }
                }
            } else {
                setIsTargetedColorLockedIn(true)
                updateColorPickerButtons(true)
            }
        }

        binding.revertPickBtn.setOnClickListener {
            setIsTargetedColorLockedIn(false)
            updateColorPickerButtons(false)
        }

        binding.closeBtn.setOnClickListener {
            navigateToLightConfigurationFragment()
        }

        binding.openGalleryBtn.setOnClickListener {
            analyticsService.trackOpenGalleryColorPickerMode()
            handleGalleryModePermissions()
        }

        binding.switchToCameraBtn.setOnClickListener {
            if (viewModel.isInCameraMode && hasCameraPermission) return@setOnClickListener
            analyticsService.trackOpenCameraColorPickerMode()

            handleCameraModePermissions()
        }
    }

    private fun trackUseSelectedColorEvent(@ColorInt colorInt: Int) {
        analyticsService.trackUseSelectedColorEvent(colorInt.colorHue.toInt(), colorInt.colorSaturation, viewModel.isInCameraMode)
    }

    private fun storeColorIndicatorContainerInitialCoordinates() {
        binding.targetedColorIndicatorContainerLL.post {
            colorIndicatorContainerInitialX = binding.targetedColorIndicatorContainerLL.x
            colorIndicatorContainerInitialY = binding.targetedColorIndicatorContainerLL.y
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUIStateForColorPickerMode(isInCameraMode: Boolean) {
        updateMissingImageUIState(false)
        binding.cameraPreview.isInvisible = !isInCameraMode
        binding.selectedImageIV.isInvisible = isInCameraMode
        if (hasCameraPermission) {
            binding.switchToCameraBtn.isInvisible = isInCameraMode
        }
        updateColorPickerButtons(!isInCameraMode)
        if (isInCameraMode) {
            binding.pixelCrossHairIV.setOnTouchListener(null)
            binding.selectedImageIV.setOnTouchListener(null)
            resetTargetedColorIndicator()
        } else {
            binding.pixelCrossHairIV.setOnTouchListener(onCrossHairDragListener)
            binding.selectedImageIV.setOnTouchListener(onSelectedImageTouchListener)
            binding.revertPickBtn.isInvisible = true
        }
    }

    private fun setCrosshairTargetedPreviewColor() {
        (binding.selectedImageIV.drawable as? BitmapDrawable)?.bitmap?.let { bitmap ->
            val crossHairDistanceToMiddlePoint = binding.pixelCrossHairIV.layoutParams.width / 2
            val crossHairCenterX =
                binding.targetedColorIndicatorContainerLL.x + binding.pixelCrossHairIV.x + crossHairDistanceToMiddlePoint
            val crossHairCenterY =
                binding.targetedColorIndicatorContainerLL.y + binding.pixelCrossHairIV.y + crossHairDistanceToMiddlePoint

            val selectedImageInvertedMatrix = Matrix()
            val bitmapPixelCoordinates = floatArrayOf(crossHairCenterX, crossHairCenterY)
            binding.selectedImageIV.imageMatrix.invert(selectedImageInvertedMatrix)
            selectedImageInvertedMatrix.mapPoints(bitmapPixelCoordinates)

            val bitmapPixelX = bitmapPixelCoordinates[BITMAP_PIXEL_COORDINATES_ARRAY_X_INDEX].roundToInt()
            val bitmapPixelY = bitmapPixelCoordinates[BITMAP_PIXEL_COORDINATES_ARRAY_Y_INDEX].roundToInt()

            val targetedColor = bitmap.getColorAt(bitmapPixelX, bitmapPixelY)
            selectedColorInt = targetedColor
            binding.colorPreviewIV.imageTintList = ColorStateList.valueOf(targetedColor)
        }
    }

    private fun resetTargetedColorIndicator() {
        binding.targetedColorIndicatorContainerLL.x = colorIndicatorContainerInitialX
        binding.targetedColorIndicatorContainerLL.y = colorIndicatorContainerInitialY
        binding.colorPreviewIV.updateLayoutParams {
            height = resources.getDimension(R.dimen.image_color_picker_color_preview_size).roundToInt()
            width = resources.getDimension(R.dimen.image_color_picker_color_preview_size).roundToInt()
        }
    }

    private fun updateColorPickerButtons(
        isTargetedColorLockedIn: Boolean
    ) {
        @ColorRes val buttonBackgroundColorRes: Int
        @ColorRes val buttonTextColor: Int
        @StringRes val buttonText: Int

        if (isTargetedColorLockedIn) {
            buttonBackgroundColorRes = R.color.bg_button_color_states
            buttonTextColor = R.color.button_text_color
            buttonText = R.string.image_color_picker_use_color_button_text
        } else {
            buttonBackgroundColorRes = R.color.bg_pick_color_button_color_states
            buttonTextColor = R.color.image_color_picker_pick_button_text_color
            buttonText = R.string.image_color_picker_pick_color_button_text
        }

        binding.confirmColorBtn.setButtonBackgroundColorTint(resources.getColor(buttonBackgroundColorRes, null))
        binding.confirmColorBtn.setTextColor(resources.getColor(buttonTextColor, null))
        binding.confirmColorBtn.setButtonText(getString(buttonText))

        binding.revertPickBtn.isInvisible = !isTargetedColorLockedIn
    }

    private fun setIsTargetedColorLockedIn(isTargetedColorLockedIn: Boolean) {
        viewModel.isTargetedColorLockedIn.set(isTargetedColorLockedIn)
    }

    private fun getHasCameraPermissions(): Boolean = requireContext().checkSelfPermission(CAMERA) == PERMISSION_GRANTED

    private fun getHasReadStoragePermissions(): Boolean = if (SDK_INT < TIRAMISU) {
        requireContext().checkSelfPermission(READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED
    } else {
        requireContext().checkSelfPermission(READ_MEDIA_IMAGES) == PERMISSION_GRANTED
    }

    private fun initRequestCameraPermissionLauncher(): ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                setIsTargetedColorLockedIn(false)
                setUIStateForColorPickerMode(true)
                startCamera()
            } else {
                dialogHandler.showPermissionsRationaleDialog(
                    requireContext(),
                    getString(R.string.rationale_dialog_camera_permissions_title),
                    getString(R.string.rationale_dialog_camera_permissions_description),
                    onDismiss = {
                        updateMissingImageUIState(
                            true,
                            getString(R.string.image_color_picker_denied_camera_permissions_instructions)
                        )
                    }
                )
            }
            viewModel.isInCameraMode = true
            binding.confirmColorBtn.isEnabled = isGranted
        }

    private fun initRequestReadStoragePermissionLauncher(): ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                viewModel.isInCameraMode = false
                cameraProvider?.unbindAll()
                getImageFromGalleryRequestLauncher.launch(STORAGE_MEDIA_TYPE_REQUEST)
            } else {
                dialogHandler.showPermissionsRationaleDialog(
                    requireContext(),
                    getString(R.string.rationale_dialog_gallery_permissions_title),
                    getString(R.string.rationale_dialog_gallery_permissions_description),
                    onDismiss = {
                        if (!viewModel.isInCameraMode) {
                            updateMissingImageUIState(
                                true,
                                getString(R.string.image_color_picker_denied_gallery_permissions_instructions)
                            )
                        }
                    }
                )
            }
        }

    private fun initGetImageFromGalleryRequestLauncher() = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        cameraProvider?.unbindAll()
        viewModel.isInCameraMode = false
        setUIStateForColorPickerMode(false)
        resetTargetedColorIndicator()
        reconnectToLights()
        Glide.with(binding.selectedImageIV)
            .load(uri)
            .listener(object : SimpleGlideRequestListener() {

                override fun onLoadFailed(): Boolean {
                    if (viewModel.isInCameraMode) return false

                    updateMissingImageUIState(
                        true,
                        getString(R.string.image_color_picker_no_image_selected_from_gallery)
                    )
                    return false
                }

                override fun onResourceReady(resource: Drawable?): Boolean {
                    if (viewModel.isInCameraMode) return false

                    if (resource == null) {
                        updateMissingImageUIState(
                            true,
                            getString(R.string.image_color_picker_no_image_selected_from_gallery)
                        )
                        return false
                    }
                    val centerColor =
                        (resource as? BitmapDrawable)?.getCenterPixelColor() ?: Color.valueOf(MAX_RGB_COMPONENT_VALUE).toArgb()
                    selectedColorInt = centerColor
                    binding.colorPreviewIV.imageTintList = ColorStateList.valueOf(centerColor)
                    return false
                }
            })
            .into(binding.selectedImageIV)
    }

    private fun reconnectToLights() {
        val lightDetails = args.lightConfigurationDetails
        if (lightDetails.configuration.isIndividualLight) {
            bluetoothService.connect(lightDetails.address!!)
        } else {
            lightDetails.configuration.lightsDetails.forEach {
                bluetoothService.connect(it.address!!)
            }
        }
    }

    private fun updateMissingImageUIState(isInformativeTextVisible: Boolean, informativeText: String? = null) {
        binding.missingImageInformationalTV.isInvisible = !isInformativeTextVisible
        binding.missingImageInformationalTV.text = informativeText
        binding.targetedColorIndicatorContainerLL.isInvisible = isInformativeTextVisible
        binding.confirmColorBtn.setIsEnabled(!isInformativeTextVisible)
    }

    private fun requestCameraPermissions() {
        if (shouldShowRequestPermissionRationale(CAMERA)) {
            if (viewModel.isInCameraMode) {
                setUIStateForColorPickerMode(true)
                updateMissingImageUIState(
                    true,
                    getString(R.string.image_color_picker_denied_camera_permissions_instructions)
                )
            }
            dialogHandler.showPermissionsRationaleDialog(
                requireContext(),
                getString(R.string.rationale_dialog_camera_permissions_title),
                getString(R.string.rationale_dialog_camera_permissions_description),
                {
                    viewModel.isInCameraMode = true
                    onOpenSettingsButtonClick()
                }
            )
        } else {
            viewModel.shouldCheckPermissions = false
            requestCameraPermissionLauncher.launch(CAMERA)
        }
    }

    private fun requestGalleryPermissions() {
        val permission = if (SDK_INT < TIRAMISU) {
            READ_EXTERNAL_STORAGE
        } else {
            READ_MEDIA_IMAGES
        }
        if (shouldShowRequestPermissionRationale(permission)) {
            if (!viewModel.isInCameraMode) {
                setUIStateForColorPickerMode(false)
                updateMissingImageUIState(
                    true,
                    getString(R.string.image_color_picker_denied_gallery_permissions_instructions)
                )
            }
            dialogHandler.showPermissionsRationaleDialog(
                requireContext(),
                getString(R.string.rationale_dialog_gallery_permissions_title),
                getString(R.string.rationale_dialog_gallery_permissions_description),
                {
                    viewModel.isInCameraMode = false
                    onOpenSettingsButtonClick()
                }
            )
        } else {
            viewModel.shouldCheckPermissions = false
            requestReadStoragePermissionLauncher.launch(permission)
        }
    }

    private fun navigateToLightConfigurationFragment() {
        findNavController().navigate(
            ImageColorPickerFragmentDirections.actionImageColorPickerFragmentToLightConfigurationDialogFragment(
                selectedLightId
            )
        )
    }

    private fun setupOnBackPressedCallback() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {
                navigateToLightConfigurationFragment()
            }
        })
    }

    private fun onOpenSettingsButtonClick() {
        viewModel.shouldCheckPermissions = true
        openAppSettings()
    }

    private fun handleSystemBarsOverlaps() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.closeBtn.updateLayoutParams<MarginLayoutParams> {
                topMargin = insets.top
            }
            CONSUMED
        }
    }


    private fun initCameraExecutor(): ExecutorService = Executors.newSingleThreadExecutor()

    companion object {
        private const val STORAGE_MEDIA_TYPE_REQUEST = "image/*"
        private const val BITMAP_PIXEL_COORDINATES_ARRAY_X_INDEX = 0
        private const val BITMAP_PIXEL_COORDINATES_ARRAY_Y_INDEX = 1
        private const val DEFAULT_PREVIEW_COLOR = WHITE
        private const val MIN_MOVE_DISTANCE_FOR_ANIMATION_START_THRESHOLD = 10

        private fun Bitmap.getColorAt(x: Int, y: Int): Int =
            if (x < 0 || y < 0 || x >= width || y >= height) {
                DEFAULT_PREVIEW_COLOR
            } else {
                getPixel(x, y).colorWithMaxBrightness
            }

        private fun BitmapDrawable.getCenterPixelColor(): Int {
            bitmap?.let { bitmap ->
                return bitmap.getColorAt(bitmap.width / 2, bitmap.height / 2)
            }
            return DEFAULT_PREVIEW_COLOR
        }
    }
}