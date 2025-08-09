package com.sliteptyltd.slite.utils.bindingadapters

import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.databinding.BindingAdapter
import com.sliteptyltd.slite.R
import com.sliteptyltd.slite.data.model.extensions.getLightsGroupStatusText
import com.sliteptyltd.slite.data.model.light.LightConfiguration
import com.sliteptyltd.slite.data.model.light.LightStatus
import com.sliteptyltd.slite.data.model.light.LightStatus.ON
import com.sliteptyltd.slite.feature.lights.adapter.data.LightsSectionType
import com.sliteptyltd.slite.feature.lights.adapter.data.LightsSectionType.GROUPS
import com.sliteptyltd.slite.feature.lights.adapter.data.LightsSectionType.INDIVIDUAL
import kotlin.math.roundToInt

@BindingAdapter("lightName", "lightStatus", requireAll = true)
fun TextView.setLightName(lightName: String, lightStatus: LightStatus) {
    text = lightName

    val textColorRes = if (lightStatus == ON) {
        R.color.lights_list_light_on_item_title_text_color
    } else {
        R.color.lights_list_light_off_item_title_text_color
    }
    setTextColor(resources.getColor(textColorRes, null))

    updateLayoutParams<ViewGroup.MarginLayoutParams> {
        marginEnd = if (lightStatus == ON) {
            resources.getDimension(R.dimen.light_details_card_title_margin_end).roundToInt()
        } else {
            resources.getDimension(R.dimen.horizontal_guideline).roundToInt()
        }
    }
}

@BindingAdapter("lightsSectionType", "lightsSectionSize", requireAll = true)
fun TextView.setLightsSectionHeaderText(lightsSectionType: LightsSectionType, lightsSectionSize: Int) {
    text = when (lightsSectionType) {
        INDIVIDUAL -> resources.getString(R.string.lights_list_section_individual_header_text_format, lightsSectionSize)
        GROUPS -> resources.getString(R.string.lights_list_section_group_header_text_format, lightsSectionSize)
    }
}

@BindingAdapter("lightConfiguration", "singleLightStatus", requireAll = true)
fun TextView.setLightStatusText(lightConfiguration: LightConfiguration, singleLightStatus: LightStatus) {
    text = if (lightConfiguration.isIndividualLight) {
        singleLightStatus.getSingleLightStatusText(resources)
    } else {
        lightConfiguration.lightsDetails.getLightsGroupStatusText(resources)
    }
}