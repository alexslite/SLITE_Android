package com.sliteptyltd.persistence.di

import android.app.Application
import androidx.room.Room
import com.sliteptyltd.persistence.Constants.SLITE_DATABASE_NAME
import com.sliteptyltd.persistence.SliteDatabase
import com.sliteptyltd.persistence.group.GroupedLightsDao
import com.sliteptyltd.persistence.group.GroupsDao
import com.sliteptyltd.persistence.light.LightsDao
import com.sliteptyltd.persistence.scene.SceneLightsDao
import com.sliteptyltd.persistence.scene.ScenesDao
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val databaseModule = module {
    single { provideSliteDatabase(androidApplication()) }
    factory { getLightsDao(sliteDatabase = get()) }
    factory { getGroupsDao(sliteDatabase = get()) }
    factory { getGroupedLightsDao(sliteDatabase = get()) }
    factory { getScenesDao(sliteDatabase = get()) }
    factory { getSceneLightsDao(sliteDatabase = get()) }
}

private fun provideSliteDatabase(application: Application): SliteDatabase =
    Room.databaseBuilder(
        application,
        SliteDatabase::class.java,
        SLITE_DATABASE_NAME
    )
        .fallbackToDestructiveMigration()
        .build()

private fun getLightsDao(sliteDatabase: SliteDatabase): LightsDao = sliteDatabase.lightsDao

private fun getGroupsDao(sliteDatabase: SliteDatabase): GroupsDao = sliteDatabase.groupsDao

private fun getGroupedLightsDao(sliteDatabase: SliteDatabase): GroupedLightsDao = sliteDatabase.groupedLightsDao

private fun getScenesDao(sliteDatabase: SliteDatabase): ScenesDao = sliteDatabase.scenesDao

private fun getSceneLightsDao(sliteDatabase: SliteDatabase): SceneLightsDao = sliteDatabase.sceneLightsDao