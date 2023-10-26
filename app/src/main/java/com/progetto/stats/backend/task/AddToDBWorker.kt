package com.progetto.stats.backend.task

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.progetto.stats.backend.database.CalculatedStatsDB
import com.progetto.stats.backend.database.StatsDB
import com.progetto.stats.backend.util.UsageStats
import kotlinx.coroutines.*
import java.lang.Long
import kotlin.Exception
import kotlin.String

class AddToDBWorker(val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    private val uStats = UsageStats
    private val statsDB = StatsDB(context, null)
    private val calculatedStatsDB = CalculatedStatsDB(context, null)
    private val packageManager = context.packageManager


    override suspend fun doWork(): Result {
        return try {
            setForeground(createForegroundInfo("Aggiunta al database in corso"))
            addToDB()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun createForegroundInfo(progress: String): ForegroundInfo {
        val id = "channelId"
        val title = "Aggiunta al database"
        val cancel = "Annulla"
        val notificationId = 1
        // intent per annullamento
        val intent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(getId())
        // canale di notifica
        val channel = NotificationChannel(id, title, NotificationManager.IMPORTANCE_HIGH)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(applicationContext, id)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(progress)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_delete, cancel, intent)
            .build()

        return ForegroundInfo(notificationId, notification)
    }



    @SuppressLint("NewApi")
    private fun addToDB(){
        Log.d("debug_tag","-ADD")
        //prendo le nuove statistiche con UsageStatsManager
        val stats=uStats.getUsageStatsList(context)
        //prendo le precedenti statistiche dal db
        val oldStatsList=statsDB.getAllUsageStats()

        //svuoto i db
        statsDB.deleteAllRows()
        calculatedStatsDB.deleteAllRows()

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
                statsDB.addUsageStats(
                    appName = appName,
                    totalTimeInForeground = totalTimeInForeground,
                    totalTimeInBackground = totalTimeInBackground,
                    lastTimeStamp = lastTimeStamp,
                    packageName = stat.key
                )
                // se c'era una statistica di utilizzo con lo stesso appName la restituisco
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
            calculatedStatsDB.addUsageStats(
                appName = appName,
                totalTimeInForeground = newForegroundTime,
                totalTimeInBackground = newBackgroundTime,
                lastTimeStamp = lastTimeStamp,
                packageName = stat.key
            )
        }

        //salvo la lista su file CSV
        val dbStatsToCSV = CSVWriter.DbStatsToCSV(calculatedStatsDB.getAllUsageStats(), context)
        dbStatsToCSV.write()

    }
}