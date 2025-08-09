package com.sliteptyltd.slite.utils

inline fun Any.logMe(
    extraTag: String? = null,
    tag: String = this::class.java.simpleName,
    crossinline msg: () -> String
) = LogMe.log(tag, extraTag, msg())

fun Any.logMe(
    extraTag: String? = null,
    tag: String = this::class.java.simpleName,
) = LogMe.log(tag, extraTag, this.toString())

object LogMe {
    private var isEnabled: Boolean = false

    fun setLoggingEnabled(enabled: Boolean) {
        this.isEnabled = enabled
    }

    fun log(tag: String, extraTag: String?, msg: String?) {
        if (!isEnabled) return

        val t = if (extraTag == null) "[$tag]" else "[$tag / $extraTag]"
        println("$t $msg")
    }
}