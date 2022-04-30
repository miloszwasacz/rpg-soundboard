package com.gmail.dev.wasacz.rpgsoundboard.model

import android.content.Context
import com.gmail.dev.wasacz.rpgsoundboard.R
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileNotFoundException

class Model {
    companion object {
        private const val DATA_FILE_NAME = "songs.json"
        private const val MEDIA_STORE_VERSION_KEY = "MEDIA_STORE"

        /**
         * Fetches list of saved songs
         * @throws SerializationException Saved data is corrupted
         */
        fun getSongs(context: Context): ArrayList<Song> {
            return try {
                val dataString = context.openFileInput(DATA_FILE_NAME).bufferedReader().useLines { lines ->
                    lines.fold("") { some, text ->
                        "$some\n$text"
                    }
                }
                Json.decodeFromString(dataString)
            } catch (e: FileNotFoundException) {
                arrayListOf()
            }
        }

        fun saveSongs(context: Context, list: List<Song>) {
            val data = Json.encodeToString(list)
            File(context.filesDir, DATA_FILE_NAME).apply {
                createNewFile()
                writeText(data)
            }
        }

        private fun getLocalMediaStoreVersion(context: Context): String? {
            val prefs = context.run {
                getSharedPreferences(getString(R.string.default_shared_preferences), Context.MODE_PRIVATE)
            }
            return prefs.getString(MEDIA_STORE_VERSION_KEY, null)
        }

        private fun setLocalMediaStoreVersion(context: Context, newVersion: String) {
            val prefs = context.run {
                getSharedPreferences(getString(R.string.default_shared_preferences), Context.MODE_PRIVATE)
            }
            prefs.edit().apply {
                putString(MEDIA_STORE_VERSION_KEY, newVersion)
                apply()
            }
        }
    }
}