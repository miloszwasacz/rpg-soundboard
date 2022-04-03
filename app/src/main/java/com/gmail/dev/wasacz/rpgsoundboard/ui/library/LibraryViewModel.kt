package com.gmail.dev.wasacz.rpgsoundboard.ui.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gmail.dev.wasacz.rpgsoundboard.model.Item
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.ListViewModel
import kotlinx.coroutines.delay

class LibraryViewModel : ListViewModel<Item>() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text

    override suspend fun getList(): List<Item> {
        delay(1000)
        return List(20) { Item("item$it") }
    }
}