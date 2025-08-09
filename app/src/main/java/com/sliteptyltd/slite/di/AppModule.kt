package com.sliteptyltd.slite.di

import SliteActivityViewModel
import com.sliteptyltd.slite.analytics.AnalyticsService
import com.sliteptyltd.slite.data.LightsRepository
import com.sliteptyltd.slite.data.preference.InternalStorageManager
import com.sliteptyltd.slite.data.usecase.AddIndividualLightUseCase
import com.sliteptyltd.slite.data.usecase.CreateGroupUseCase
import com.sliteptyltd.slite.data.usecase.DeleteIndividualLightUseCase
import com.sliteptyltd.slite.data.usecase.caching.FetchLightsUseCase
import com.sliteptyltd.slite.data.usecase.GetIndividualLightsUseCase
import com.sliteptyltd.slite.data.usecase.GetLightByIdUseCase
import com.sliteptyltd.slite.data.usecase.GetLightsGroupsUseCase
import com.sliteptyltd.slite.data.usecase.RenameIndividualLightUseCase
import com.sliteptyltd.slite.data.usecase.RenameLightsGroupUseCase
import com.sliteptyltd.slite.data.usecase.UngroupLightsUseCase
import com.sliteptyltd.slite.data.usecase.UpdateLightUseCase
import com.sliteptyltd.slite.data.usecase.UpdateLightsSectionPowerStatusUseCase
import com.sliteptyltd.slite.data.usecase.caching.FetchScenesUseCase
import com.sliteptyltd.slite.data.usecase.caching.StoreLatestLightsListUseCase
import com.sliteptyltd.slite.data.usecase.caching.StoreLatestScenesUseCase
import com.sliteptyltd.slite.data.usecase.scenes.CreateSceneUseCase
import com.sliteptyltd.slite.data.usecase.scenes.DeleteSceneUseCase
import com.sliteptyltd.slite.data.usecase.scenes.GetCanCreateSceneUseCase
import com.sliteptyltd.slite.data.usecase.scenes.GetContainingScenesUseCase
import com.sliteptyltd.slite.data.usecase.scenes.GetScenesUseCase
import com.sliteptyltd.slite.data.usecase.scenes.RenameSceneUseCase
import com.sliteptyltd.slite.data.usecase.scenes.UpdateSceneLightsDetailsUseCase
import com.sliteptyltd.slite.feature.addlight.AddLightViewModel
import com.sliteptyltd.slite.feature.addlight.setupslite.SettingUpSliteViewModel
import com.sliteptyltd.slite.feature.effectsservice.BluetoothEffectsPlayer
import com.sliteptyltd.slite.feature.effectsservice.provider.EffectsProvider
import com.sliteptyltd.slite.feature.effectsservice.EffectsPlayer
import com.sliteptyltd.slite.feature.effectsservice.EffectsHandler
import com.sliteptyltd.slite.feature.groups.CreateGroupViewModel
import com.sliteptyltd.slite.feature.lightconfiguration.LightConfigurationDetails
import com.sliteptyltd.slite.feature.lightconfiguration.LightConfigurationViewModel
import com.sliteptyltd.slite.feature.lightconfiguration.imagecolor.ImageColorPickerViewModel
import com.sliteptyltd.slite.feature.lightconfiguration.photosensitivity.PhotoSensitivityViewModel
import com.sliteptyltd.slite.feature.lights.LightsViewModel
import com.sliteptyltd.slite.feature.scenes.ScenesViewModel
import com.sliteptyltd.slite.feature.updatelights.UpdateLightsViewModel
import com.sliteptyltd.slite.utils.AnnouncementHandler
import com.sliteptyltd.slite.utils.bluetooth.BluetoothService
import com.sliteptyltd.slite.utils.bluetooth.update.UpdateProvider
import com.sliteptyltd.slite.utils.handlers.DialogHandler
import com.sliteptyltd.slite.utils.handlers.InAppUpdateHandler
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModels = module {
    viewModel {
        LightsViewModel(
            fetchLightsFromLocalStorage = get(),
            storeLatestLightsList = get(),
            getIndividualLights = get(),
            getLightsGroups = get(),
            deleteIndividualLight = get(),
            renameIndividualLight = get(),
            renameLightsGroup = get(),
            ungroupLightsUseCase = get(),
            updateLight = get(),
            updateLightsSectionPowerStatus = get(),
            createScene = get(),
            storeLatestScenes = get(),
            fetchScenes = get(),
            getContainingScenes = get()
        )
    }
    viewModel { (selectedLightId: Int) ->
        LightConfigurationViewModel(
            selectedLightId,
            getLightById = get(),
            updateLight = get()
        )
    }
    viewModel { (lightConfigurationDetails: LightConfigurationDetails) ->
        ImageColorPickerViewModel(
            lightConfigurationDetails,
            updateLight = get()
        )
    }
    viewModel {
        CreateGroupViewModel(
            getIndividualLights = get(),
            createGroup = get(),
            getContainingScenes = get()
        )
    }
    viewModel {
        ScenesViewModel(
            storeLatestScenes = get(),
            getScenes = get(),
            createScene = get(),
            getCanCreateScene = get(),
            renameScene = get(),
            deleteScene = get(),
            updateSceneLightsDetails = get(),
            get(),
            get()
        )
    }
    viewModel {
        SettingUpSliteViewModel(
            addIndividualLight = get(),
            storeLatestLightsList = get()
        )
    }
    viewModel {
        UpdateLightsViewModel(
            getIndividualLightsUseCase = get(),
            bluetoothService = get()
        )
    }

    viewModel { AddLightViewModel() }

    viewModel { PhotoSensitivityViewModel(updateLight = get()) }

    viewModel { SliteActivityViewModel(storeLatestLightsList = get()) }
}

val internalStorageManager = module {
    single { InternalStorageManager(androidContext()) }
}

val handlers = module {
    factory { DialogHandler() }
    factory { AnnouncementHandler() }
    single { EffectsHandler(androidApplication()) }
    single { InAppUpdateHandler() }
}

val services = module {
    single { BluetoothService(androidContext()) }
    single<EffectsPlayer> { BluetoothEffectsPlayer() }
    single { AnalyticsService() }
}

val providers = module {
    single { EffectsProvider() }
    factory { UpdateProvider(androidContext()) }
}

val repositories = module {
    single {
        LightsRepository(
            lightsDao = get(),
            groupsDao = get(),
            groupedLightsDao = get(),
            scenesDao = get(),
            sceneLightsDao = get()
        )
    }
}

val useCases = module {
    factory { GetIndividualLightsUseCase(lightsRepository = get()) }
    factory { GetLightsGroupsUseCase(lightsRepository = get()) }
    factory { GetLightByIdUseCase(lightsRepository = get()) }
    factory { AddIndividualLightUseCase(lightsRepository = get()) }
    factory { DeleteIndividualLightUseCase(lightsRepository = get()) }
    factory { UngroupLightsUseCase(lightsRepository = get()) }
    factory { UpdateLightUseCase(lightsRepository = get()) }
    factory { UpdateLightsSectionPowerStatusUseCase(lightsRepository = get()) }
    factory { RenameIndividualLightUseCase(lightsRepository = get()) }
    factory { RenameLightsGroupUseCase(lightsRepository = get()) }
    factory { CreateGroupUseCase(lightsRepository = get()) }
    factory { GetScenesUseCase(lightsRepository = get()) }
    factory { CreateSceneUseCase(lightsRepository = get()) }
    factory { GetCanCreateSceneUseCase(lightsRepository = get()) }
    factory { RenameSceneUseCase(lightsRepository = get()) }
    factory { DeleteSceneUseCase(lightsRepository = get()) }
    factory { FetchLightsUseCase(lightsRepository = get()) }
    factory { StoreLatestLightsListUseCase(lightsRepository = get()) }
    factory { FetchScenesUseCase(lightsRepository = get()) }
    factory { StoreLatestScenesUseCase(lightsRepository = get()) }
    factory { UpdateSceneLightsDetailsUseCase(lightsRepository = get()) }
    factory { GetContainingScenesUseCase(lightsRepository = get()) }
}