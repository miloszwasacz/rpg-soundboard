package com.gmail.dev.wasacz.rpgsoundboard.viewmodel

import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController

object ExceptionCodes {
    object Database {
        private const val BASE_DB_EXCEPTION_CODE: Int = 0x845DBE0
        fun DatabaseController.DBException.Cause.getCode() = BASE_DB_EXCEPTION_CODE and (ordinal + 1)
    }

    const val NAVIGATION_ARG_EXCEPTION = 0x1147A9E0
    fun getCodeString(code: Int): String = String.format("%#x", code).run {
        substring(0..1) + substring(2).uppercase()
    }
}