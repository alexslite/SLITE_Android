package com.sliteptyltd.slite.utils.extensions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

fun <T, R, X> MediatorLiveData<X>.combineNonNull(source1: LiveData<T>, source2: LiveData<R>, merger: (T, R) -> X) {
    addSource(source1) { source -> value = merger(source, source2.value ?: return@addSource) }
    addSource(source2) { source -> value = merger(source1.value ?: return@addSource, source) }
}

fun <T, R, S, V, X> MediatorLiveData<X>.combineNonNull(
    source1: LiveData<T>,
    source2: LiveData<R>,
    source3: LiveData<S>,
    source4: LiveData<V>,
    merger: (T, R, S, V) -> X
) {
    addSource(source1) { source ->
        value = merger(
            source,
            source2.value ?: return@addSource,
            source3.value ?: return@addSource,
            source4.value ?: return@addSource,
        )
    }
    addSource(source2) { source ->
        value = merger(
            source1.value ?: return@addSource,
            source,
            source3.value ?: return@addSource,
            source4.value ?: return@addSource,
        )
    }
    addSource(source3) { source ->
        value = merger(
            source1.value ?: return@addSource,
            source2.value ?: return@addSource,
            source,
            source4.value ?: return@addSource,
        )
    }
    addSource(source4) { source ->
        value = merger(
            source1.value ?: return@addSource,
            source2.value ?: return@addSource,
            source3.value ?: return@addSource,
            source,
        )
    }
}