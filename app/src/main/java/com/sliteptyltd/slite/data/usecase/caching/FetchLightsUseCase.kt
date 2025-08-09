package com.sliteptyltd.slite.data.usecase.caching

import com.sliteptyltd.slite.data.LightsRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class FetchLightsUseCase(
    private val lightsRepository: LightsRepository
) {

    suspend operator fun invoke(): Boolean =
        withContext(IO) {
            lightsRepository.fetchLights()
        }
}