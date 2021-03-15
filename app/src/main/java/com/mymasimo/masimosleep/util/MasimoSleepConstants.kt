package com.mymasimo.masimosleep.util

import android.content.Context
import android.content.Intent
import androidx.annotation.Size
import androidx.core.content.FileProvider
import com.masimo.common.logging.Log
import com.masimo.common.logging.TaggedLog
import com.masimo.common.model.universal.ExceptionID
import com.masimo.common.model.universal.ParameterID
import com.mymasimo.masimosleep.BuildConfig.APPLICATION_ID
import java.io.File
import java.util.*

const val DEFAULT_MANUFACTURER_NAME = "MASIMO"

//Minimal disk space
const val MIN_DISK_SPACE = 100 //100MB

const val FILE_PROVIDER_AUTHORITY = "$APPLICATION_ID.shareout"
const val SINGLE_PROFILE_MODE_ENABLED = true

private const val LOG_TAG_PREFIX = "OSN:"
private const val MAX_TAG_LEN = 25 - LOG_TAG_PREFIX.length // 25 enforced by android.util.Log

fun taggedLog(@Size(min = 1L, max = MAX_TAG_LEN.toLong()) name: String): TaggedLog {
    if (name.isBlank()) throw java.lang.IllegalArgumentException("Tag name empty")
    val tag = if (name.length > MAX_TAG_LEN) name.substring(0, MAX_TAG_LEN - 1) else name
    return Log.tag("$LOG_TAG_PREFIX$tag")
}

val SUPPORTED_PARAMETER_EXCEPTION_MASK_MAP = EnumMap<ExceptionID, Int>(ExceptionID::class.java).apply {
    //BitMasks for Serializing Parameter Exception Data
    this[ExceptionID.LOW_CONFIDENCE] = 1
    this[ExceptionID.INVALID] = 1 shl 1
    this[ExceptionID.STARTUP] = 1 shl 2
    this[ExceptionID.INVALID_SMOOTH_PI] = 1 shl 3
    this[ExceptionID.SMOOTH_PI_STARTUP_STATE] = 1 shl 4
}.toMap()

val DISPLAYABLE_PARAMETER_AVERAGES: EnumSet<ParameterID> = EnumSet.noneOf(ParameterID::class.java).apply {
    add(ParameterID.FUNC_SPO2)
    add(ParameterID.PR)
    add(ParameterID.PI)
}

internal fun externalShareIntent(context: Context, mime: String?, subject: String?, text: String?, vararg files: File): Intent {
    val intent = when {
        files.size > 1 -> Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            putExtra(Intent.EXTRA_STREAM, ArrayList(files.map { FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, it) }))
        }
        files.size == 1 -> Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, files[0]))
        }
        else -> throw IllegalArgumentException("No files provided")
    }

    mime?.let { intent.type = it }
    subject?.let { intent.putExtra(Intent.EXTRA_SUBJECT, it) }
    text?.let { intent.putExtra(Intent.EXTRA_TEXT, it) }

    return intent
}