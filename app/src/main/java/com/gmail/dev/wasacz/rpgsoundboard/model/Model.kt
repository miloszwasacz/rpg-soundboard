package com.gmail.dev.wasacz.rpgsoundboard.model

import android.content.Context
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileNotFoundException

class Model {
    companion object {
        private const val DATA_FILE_NAME = ""

        /**
         * @throws SerializationException saved data is corrupted
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
                if(exists()) delete()
                createNewFile()
                writeText(data)
            }
        }
    }
}