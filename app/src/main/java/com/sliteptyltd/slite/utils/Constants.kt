package com.sliteptyltd.slite.utils

import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build.VERSION_CODES.S
import androidx.annotation.RequiresApi
import com.sliteptyltd.slite.utils.color.transformTemperatureToRGB

object Constants {

    object Lights {
        const val LIGHT_NAME_MIN_LENGTH = 1
        const val LIGHT_NAME_MAX_LENGTH = 15

        const val LIGHT_CONFIGURATION_MIN_BRIGHTNESS = 0L
        const val LIGHT_CONFIGURATION_MAX_BRIGHTNESS = 100L

        const val LIGHT_CONFIGURATION_MIN_SATURATION = 0L
        const val LIGHT_CONFIGURATION_MAX_SATURATION = 100L

        const val LIGHT_CONFIGURATION_MIN_WHITE_TEMPERATURE = 2_500L
        const val LIGHT_CONFIGURATION_MAX_WHITE_TEMPERATURE = 10_000L

        const val LIGHT_BATTERY_MIN_PERCENTAGE = 0.0
        const val LIGHT_BATTERY_MAX_PERCENTAGE = 100.0

        const val GROUP_LIGHTS_MULTIPLE_STATUSES_FORMAT = "%s • %s"

        const val INDIVIDUAL_LIGHTS_CONTROL_ITEM_ID = 1001
        const val LIGHTS_GROUP_CONTROL_ITEM_ID = 2001
        const val INDIVIDUAL_LIGHTS_HEADER_ID = 3001
        const val LIGHTS_GROUP_HEADER_ID = 4001

        const val EXPAND_LIGHT_ITEM_ANIMATION_DURATION = 200L
        const val LIGHT_ITEM_PERCENT_TEXT_FORMAT = "%d%%"
        const val LIGHT_TEMPERATURE_TEXT_FORMAT = "%dK"

        const val DROPDOWN_ROTATION_DURATION = 200L
        const val DROPDOWN_EXPANDED_ROTATION = -180f
        const val DROPDOWN_COLLAPSED_ROTATION = 0f


        const val SLITE_STATUS_BLACKOUT: Byte = 1
        const val SLITE_STATUS_ON: Byte = 0

        const val DEFAULT_LIGHT_HUE = 1f
        const val DEFAULT_LIGHT_BRIGHTNESS = 5
        const val DEFAULT_LIGHT_TEMPERATURE = 5600
        const val DEFAULT_LIGHT_SATURATION = 100
        const val DEFAULT_LIGHT_BLACKOUT_STATE: Byte = SLITE_STATUS_ON

        const val GRADIENT_TRACK_HEIGHT_DIFFERENCE = 0.5f

        const val DEFAULT_RESIZE_ANIMATION_DURATION = 200L
    }

    object Effects {
        val defaultEffectGradientColors = intArrayOf(Color.parseColor("#FFFFFF"), Color.parseColor("#FFFFFF"))
        val faultyGlobeEffectGradientColors = intArrayOf(Color.parseColor("#FFFFFF"), Color.parseColor("#F2F1BD"))
        val paparazziEffectGradientColors = intArrayOf(Color.parseColor("#FFFFFF"), Color.parseColor("#DCE5FD"))
        val tvEffectGradientColors = intArrayOf(Color.parseColor("#4a00ff"), Color.parseColor("#15b126"), Color.parseColor("#ff00cc"))
        val lightningEffectGradientColors =
            intArrayOf(Color.parseColor("#e6d1ff"), Color.parseColor("#fdfcff"), Color.parseColor("#3f3194"))
        val policeEffectGradientColors = intArrayOf(Color.parseColor("#000CF7"), Color.parseColor("#FF0101"))
        val fireworksEffectGradientColors = intArrayOf(Color.parseColor("#F53B83"), Color.parseColor("#1182F5"))
        val discoEffectGradientColors = intArrayOf(Color.parseColor("#E01E00"), Color.parseColor("#0CEDD7"))
        val pulsingEffectGradientColors = intArrayOf(Color.parseColor("#FFFFFF"), Color.parseColor("#E84C0C"))
        val strobeEffectGradientColors = intArrayOf(Color.parseColor("#000000"), Color.parseColor("#FFFFFF"))
        val fireEffectGradientColors = intArrayOf(Color.parseColor("#E04201"), Color.parseColor("#F7BE0C"))

        val defaultEffectGradientOrientation = GradientDrawable.Orientation.LEFT_RIGHT
        val fireworksEffectGradientOrientation = GradientDrawable.Orientation.TL_BR
        val fireEffectGradientOrientation = GradientDrawable.Orientation.BOTTOM_TOP

        const val EFFECT_MIN_REPEAT_COUNT = 1
        const val DAYLIGHT_WHITE_KELVIN_VALUE = 5600
        val daylightWhite = transformTemperatureToRGB(DAYLIGHT_WHITE_KELVIN_VALUE)
    }

    object Groups {
        const val MINIMUM_LIGHTS_COUNT_IN_GROUP = 2
    }

    object ColorUtils {
        const val MIN_RGB_COMPONENT_VALUE = 0
        const val MAX_RGB_COMPONENT_VALUE = 255

        const val MIN_HUE_VALUE = 0f
        const val MAX_HUE_VALUE = 359f

        const val MIN_EFFECT_HUE_VALUE = 0
        const val MAX_EFFECT_HUE_VALUE = 359
        const val MIN_EFFECT_SATURATION_VALUE = 0
        const val MAX_EFFECT_SATURATION_VALUE = 100

        const val MAX_RED_TEMPERATURE_THRESHOLD = 66f
        const val MAX_BLUE_TEMPERATURE_THRESHOLD = 66f
        const val MIN_BLUE_TEMPERATURE_THRESHOLD = 19f
        const val GREEN_FORMULA_DIFFERENTIATION_THRESHOLD = 66f

        const val HSV_ARRAY_SIZE = 3
        const val HSV_ARRAY_HUE_INDEX = 0
        const val HSV_ARRAY_SATURATION_INDEX = 1
        const val HSV_ARRAY_VALUE_INDEX = 2
        const val HSV_MAX_SATURATION = 1f
        const val HSV_MIN_SATURATION = 0f
        const val HSV_MAX_VALUE = 1f
        const val HSV_MIN_VALUE = 0f

        const val COLOR_HEX_STRING_ARGB_FORMAT = "#%08X"
        const val COLOR_HEX_STRING_RGB_FORMAT = "#%06X"
        const val COLOR_HEX_PREFIX = "#"
        const val COLOR_HEX_REGEX = "[A-Fa-f0-9]{6}"

        const val EFFECT_DICE_ROLL_MIN_VALUE = 1
        const val EFFECT_DICE_ROLL_MAX_VALUE = 100
        val effectBlackoutColor = Color.rgb(MIN_RGB_COMPONENT_VALUE, MIN_RGB_COMPONENT_VALUE, MIN_RGB_COMPONENT_VALUE)
    }

    object ImageAnalysis {
        const val RGB_PLANE_PROXY_INDEX = 0
        const val RGBA_8888_GREEN_OFFSET = 1
        const val RGBA_8888_BLUE_OFFSET = 2
        const val COLOR_SQUARE_SIZE = 11
        const val COLOR_SQUARE_MID_ROW_INDEX = (COLOR_SQUARE_SIZE + 1) / 2
        const val COLOR_SQUARE_MID_COLUMN_INDEX = (COLOR_SQUARE_SIZE + 1) / 2
        const val COLOR_SQUARE_AREA = COLOR_SQUARE_SIZE * COLOR_SQUARE_SIZE
    }

    object BottomNavigation {
        const val CONSTRAINT_SET_CHANGE_INTERPOLATOR_TENSION = 1f
    }

    object Connectivity {
        const val LIGHT_CONFIGURATION_UPDATE_THROTTLE = 250L
        const val LIGHT_CONFIGURATION_SLIDER_UPDATE_THROTTLE = 100L
        const val SLITE_NAME_FILTERING_PREFIX = "Slite"

        @RequiresApi(S)
        val MIN_SDK_31_BLUETOOTH_PERMISSIONS = arrayOf(BLUETOOTH_SCAN, BLUETOOTH_CONNECT)

        const val SLITE_CHARACTERISTIC_TEMPERATURE_INDEX = 0
        const val SLITE_CHARACTERISTIC_HUE_INDEX = 1
        const val SLITE_CHARACTERISTIC_SATURATION_INDEX = 2
        const val SLITE_CHARACTERISTIC_BRIGHTNESS_INDEX = 3
        const val SLITE_CHARACTERISTIC_BLACKOUT_INDEX = 4
        const val SLITE_CHARACTERISTICS_SEPARATOR = ','
    }

    object Support {
        const val URL_CLICKABLE_SPAN_START_INDEX = 2
        const val URL_COLOR_SPAN_START_INDEX = 0

        const val PRIVACY_POLICY_URL = "https://slite.co/pages/application-privacy-policy"
        const val TERMS_OF_USE_URL = "https://slite.co/pages/application-terms-conditions"
        const val CONTACT_ADDRESS_URI_STRING = "mailto:hello@slite.co"

        const val PLAY_STORE_APP_URL = "https://play.google.com/store/apps/details?id=com.sliteptyltd.slite"
    }

    object Sentry {
        const val DSN = "https://81a48b7e186a4824a005ce903e7d1d6a@o1297260.ingest.sentry.io/6525594"
        const val TRACES_SAMPLE_RATE = 1.0
    }

    const val SETTINGS_URI_SCHEME = "package"
    const val GRADIENT_MIN_COLOR_COUNT = 2
}