package com.sliteptyltd.persistence.light

data class LightEntityConfiguration(
    val hue: Float,
    val brightness: Int,
    val temperature: Int,
    val saturation: Int,
    val effect: String?,
    val configurationMode: String
)
