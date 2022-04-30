package com.gmail.dev.wasacz.rpgsoundboard.ui.generic

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout


abstract class ListViewModel<T> : ViewModel() {
    companion object {
        const val TIMEOUT: Long = 30_000
    }

    enum class ListState {
        LOADING,
        READY,
        EMPTY,
        TIMEOUT,
        SERIALIZATION_ERROR,
        INTERNAL_ERROR
    }

    data class StatefulList<T>(val list: List<T>?, val state: ListState)

    private val _list = MutableStateFlow<List<T>?>(null)
    private val _listState = MutableStateFlow(ListState.LOADING)
    val list: StateFlow<StatefulList<T>> = _list.combine(_listState) { list, state ->
        StatefulList(list, state)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, StatefulList(null, ListState.LOADING))

    fun onFragmentInit(context: Context) {
        if (_list.value == null) fetchList(context)
    }

    /**
     * Launches a job that fetches the list.
     * New list value will be emitted by the [list] flow.
     *
     * @param timeout timeout in milliseconds.
     * @see list flow with current list.
     */
    fun fetchList(context: Context, timeout: Long = TIMEOUT) {
        viewModelScope.launch {
            emitList(null, ListState.LOADING)
            try {
                val list = withTimeout(timeout) { getList(context) }
                list?.let { emitList(it, ListState.READY) }
            } catch (e: TimeoutCancellationException) {
                emitList(null, ListState.TIMEOUT)
            }
        }
    }

    /**
     * Implementation of getting a list from a source.
     * Should catch any necessary exceptions.
     * When null is returned, value will not be emitted.
     *
     * @see list
     */
    protected abstract suspend fun getList(context: Context): List<T>?

    protected fun emitList(list: List<T>?, state: ListState) {
        viewModelScope.launch {
            when (state) {
                ListState.READY,
                ListState.EMPTY -> {
                    if (list == null) {
                        _listState.value = ListState.INTERNAL_ERROR
                        _list.value = null
                    } else {
                        _listState.value = if (list.isEmpty()) ListState.EMPTY else ListState.READY
                        _list.value = list
                    }
                }
                else -> {
                    _listState.value = state
                    _list.value = null
                }
            }
        }
    }
}