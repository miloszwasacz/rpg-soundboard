package com.gmail.dev.wasacz.rpgsoundboard.ui.library.presets

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.ListViewModel
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.DatabaseViewModel
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Preset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LibraryViewModel(private val dbViewModel: DatabaseViewModel) : ListViewModel<Preset>() {
    override suspend fun getList(context: Context): List<Preset> = withContext(Dispatchers.IO) {
        dbViewModel.getPresets()
    }

    suspend fun addPreset(name: String): Pair<Long, String> = withContext(Dispatchers.IO) {
        dbViewModel.createPreset(name) to name
    }

    suspend fun deletePresets(presets: List<Preset>) = withContext(Dispatchers.IO) {
        for (preset in presets)
            dbViewModel.deletePreset(preset.id)
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val dbViewModel: DatabaseViewModel) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = LibraryViewModel(dbViewModel) as T
    }
}