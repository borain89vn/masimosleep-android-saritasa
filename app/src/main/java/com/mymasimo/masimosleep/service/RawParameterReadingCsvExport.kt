package com.mymasimo.masimosleep.service

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import com.mymasimo.masimosleep.data.room.entity.RawParameterReadingContract as Contract

/**
 * Export raw parameter readings for particular day into CSV file.
 */
object RawParameterReadingCsvExport {
    const val CSV_MIME_TYPE = "application/csv"

    private val dateTimeFormat = SimpleDateFormat("dd_MM_yyyy HH_mm_ss")

    /**
     * Export data to Downloads folder in a CSV file.
     *
     * First, write the data into app-specific folder.
     * Then, write its content into Downloads folder using MediaStore.
     * Finally, delete file from app-specific folder.
     */
    fun exportToDownloads(context: Context, startAt: Long, endAt: Long, data: List<List<String>>) : Uri {
        val filename = generateFileName(context, startAt, endAt)
        Timber.d("Export internal destination: $filename")

        val headers = listOf(
            Contract.COLUMN_CREATED_AT,
            Contract.COLUMN_NIGHT,
            Contract.COLUMN_SPO2,
            Contract.COLUMN_PR,
            Contract.COLUMN_RRP
        )
        csvWriter().open(filename) {
            writeRow(headers)
            Timber.d("Export headers $headers WRITTEN")
            writeRows(data)
            Timber.d("Export data len: ${data.size} WRITTEN")
        }

        return FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", File(filename) )
    }

    /**
     * Generate filename to store exported data in the app-specific folder.
     */
    private fun generateFileName(context: Context, startAt: Long, endAt: Long): String {
        val destDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!
        val startDate = dateTimeFormat.format(Date(startAt))
        val endDate = dateTimeFormat.format(Date(endAt))

        val filename = "Raw Sensor Data $startDate - ${endDate}.csv"

        return "${destDir}/$filename"
    }
}