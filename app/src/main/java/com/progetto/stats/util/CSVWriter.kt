package com.progetto.stats.util

import android.content.Context
import com.progetto.stats.util.db.Db_Stats
import java.io.File
import java.io.FileWriter

class CSVWriter(file: File) {
    private val writer = if (file.exists()) FileWriter(file, true) else FileWriter(file)

    fun writeLine(line: List<String>) {
        writer.append(line.joinToString(separator = ","))
        writer.append('\n')
    }

    fun close() {
        writer.flush()
        writer.close()
    }

    class DbStatsToCSV(val stats: List<Db_Stats<String, Long, Long, Long, String>>, val context: Context) {
        fun write() {
            val externalStorageDir = context.getExternalFilesDir(null)
            val fileName = "stats.csv"
            val file = File(externalStorageDir, fileName)
            val csvWriter = CSVWriter(file)

            stats.forEach { stat ->
                val line = listOf(
                    stat.appName,
                    stat.totalTimeInForeground.toString(),
                    stat.totalTimeInBackground.toString(),
                    stat.lastTimeStamp.toString(),
                    stat.packageName
                )
                csvWriter.writeLine(line)
            }
            csvWriter.close()
        }
    }
}


