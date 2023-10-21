package com.progetto.stats.util.db

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.progetto.stats.UsageStats
import com.progetto.stats.util.CSVWriter
import kotlinx.coroutines.*
import java.lang.Long
import kotlin.Exception

class AddToDBWorker(val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    private val handler = Handler(Looper.getMainLooper())
    private val uStats = UsageStats
    private val dbStats = DBStats(context, null)
    private val calculatedDBStats = CalculatedDBStats(context, null)
    private val packageManager = context.packageManager

    override suspend fun doWork(): Result {
        return try {
            withContext(Dispatchers.IO) {
                addToDB()
            }
            Log.d("debug_tag","chiamata addToDb eseguita ")
            Result.success()
        } catch (e: Exception) {
            Log.d("debug_tag","chiamata addToDb fallita ")
            Result.failure()
        }
    }

    @SuppressLint("NewApi")
    private fun addToDB(){
        //prendo le nuove statistiche con UsageStatsManager
        val stats=uStats.getUsageStatsList(context)
        //prendo le precedenti statistiche dal db
        val oldStatsList=dbStats.getAllUsageStats()

        //svuoto i db
        dbStats.deleteAllRows()
        calculatedDBStats.deleteAllRows()

        for (stat in stats) {
            val appName = try {
                val appInfo = packageManager.getApplicationInfo(stat.key, PackageManager.GET_META_DATA)
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (e: PackageManager.NameNotFoundException) {
                stat.key
            }
            val lastTimeStamp = stat.value.lastTimeStamp
            var totalTimeInForeground = stat.value.totalTimeInForeground
            var totalTimeInBackground = stat.value.totalTimeForegroundServiceUsed
            var newForegroundTime=totalTimeInForeground
            var newBackgroundTime=totalTimeInBackground
            //se il tempo tot >0
            if(totalTimeInBackground+totalTimeInForeground>0L) {
                //aggiungo (o aggiorno) al db la nuova statistica
                dbStats.addUsageStats(
                    appName = appName,
                    totalTimeInForeground = totalTimeInForeground,
                    totalTimeInBackground = totalTimeInBackground,
                    lastTimeStamp = lastTimeStamp,
                    packageName = stat.key
                )
                //controllo se c'era una statistica di utilizzo con lo stesso appName
                val oldStats = oldStatsList.firstOrNull { it.appName == appName }
                if (oldStats != null) {
                    val oldForegroundTime = oldStats.totalTimeInForeground
                    val oldBackgroundTime = oldStats.totalTimeInBackground
                    //Calcolo i nuovi valori reali
                    newForegroundTime = Long.min(
                        totalTimeInForeground,
                        Long.max(0, totalTimeInForeground - oldForegroundTime)
                    )
                    newBackgroundTime = Long.min(
                        totalTimeInBackground,
                        Long.max(0, totalTimeInBackground - oldBackgroundTime)
                    )
                }
            }
            //aggiungo la statistica al db delle statistiche calcolate
            calculatedDBStats.addUsageStats(
                appName = appName,
                totalTimeInForeground = newForegroundTime,
                totalTimeInBackground = newBackgroundTime,
                lastTimeStamp = lastTimeStamp,
                packageName = stat.key
            )
        }

        //salvo la lista su file CSV
        val dbStatsToCSV = CSVWriter.DbStatsToCSV(calculatedDBStats.getAllUsageStats(), context)
        dbStatsToCSV.write()

    }
}