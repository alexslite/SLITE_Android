package com.sliteptyltd.slite.data.preference

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

sealed class InternalStorageFieldDelegate<T>(protected val key: kotlin.String, protected val defaultValue: T) :
    ReadWriteProperty<InternalStorageFieldDelegate.StorageManager, T> {

    class Boolean(private val preferences: SharedPreferences, key: kotlin.String, defaultValue: kotlin.Boolean) :
        InternalStorageFieldDelegate<kotlin.Boolean>(key, defaultValue) {

        override fun getValue(thisRef: StorageManager, property: KProperty<*>) = preferences.getBoolean(key, defaultValue)

        override fun setValue(thisRef: StorageManager, property: KProperty<*>, value: kotlin.Boolean) {
            preferences.edit { putBoolean(key, value) }
        }
    }

    class String(private val preferences: SharedPreferences, key: kotlin.String, defaultValue: kotlin.String?) :
        InternalStorageFieldDelegate<kotlin.String?>(key, defaultValue) {

        override fun getValue(thisRef: StorageManager, property: KProperty<*>) = preferences.getString(key, defaultValue)

        override fun setValue(thisRef: StorageManager, property: KProperty<*>, value: kotlin.String?) {
            preferences.edit { putString(key, value) }
        }
    }

    class Int(private val preferences: SharedPreferences, key: kotlin.String, defaultValue: kotlin.Int) :
        InternalStorageFieldDelegate<kotlin.Int>(key, defaultValue) {

        override fun getValue(thisRef: StorageManager, property: KProperty<*>) = preferences.getInt(key, defaultValue)

        override fun setValue(thisRef: StorageManager, property: KProperty<*>, value: kotlin.Int) {
            preferences.edit { putInt(key, value) }
        }
    }

    interface StorageManager
}