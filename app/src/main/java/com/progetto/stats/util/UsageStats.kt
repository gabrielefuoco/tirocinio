package com.progetto.stats

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import java.util.Calendar


object UsageStats {


    //restituisce tutte le statistiche di utilizzo delle ultime 24 ore circa
    @RequiresApi(Build.VERSION_CODES.Q)
    fun getUsageStatsList(context: Context): Map<String, UsageStats> {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        val end = calendar.timeInMillis
        calendar.add(Calendar.HOUR_OF_DAY, -24)
        val start = calendar.timeInMillis
        val stats: Map<String, UsageStats> = usm.queryAndAggregateUsageStats(start, end)

        return stats.filter { (_, value) -> value.totalTimeVisible + value.totalTimeForegroundServiceUsed > 0L && value.lastTimeStamp>=start }
            .toList()
            .sortedByDescending { (_, value) -> value.totalTimeVisible + value.totalTimeForegroundServiceUsed }
            .toMap()
    }
}


/*
        DEPRECATO PERCHE' NON RIESCO A CALCOLARE IL TEMPO IN BACKGROUND
        fun getAppStats(context: Context, minuti: Int = 60): List<Db_Stats<String, Long, Long, Long, String>> {
        val foregroundStats = getAppInForeground(context, minuti)
        val backgroundStats = getAppInBackground(context, minuti)

        val statsList = mutableListOf<Db_Stats<String, Long, Long, Long, String>>()

        for ((packageName, foregroundTime) in foregroundStats) {
            val backgroundTime = backgroundStats[packageName] ?: 0
            val lastTimeStamp = System.currentTimeMillis()
            statsList.add(Db_Stats("", foregroundTime, backgroundTime, lastTimeStamp, packageName))
        }

        return statsList
    }

    private fun getAppInForeground(context: Context, minuti: Int = 60): Map<String, Long> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        val end = calendar.timeInMillis
        calendar.add(Calendar.MINUTE, -minuti)
        val start = calendar.timeInMillis
        val usageEventsIterator = usageStatsManager.queryEvents(start, end)
        val usageEvents = mutableMapOf<String, Long>()
        while (usageEventsIterator.hasNextEvent()) {
            val event = UsageEvents.Event()
            usageEventsIterator.getNextEvent(event)
            if (event.packageName != context.getPackageName()) {
                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                    val newTimeStamp = end - event.timeStamp
                    val lastTimeStamp = usageEvents[event.packageName] ?: 0
                    usageEvents[event.packageName] = lastTimeStamp + newTimeStamp
                } else if (event.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                    val newTimeStamp = end - event.timeStamp
                    val lastTimeStamp = usageEvents[event.packageName]!! - newTimeStamp
                    usageEvents[event.packageName] = lastTimeStamp
                }
            }
        }
        return usageEvents
    }

    private fun getAppInBackground(context: Context, minuti: Int = 60): Map<String, Long> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        val end = calendar.timeInMillis
        calendar.add(Calendar.MINUTE, -minuti)
        val start = calendar.timeInMillis
        val usageEventsIterator = usageStatsManager.queryEvents(start, end)
        val usageEvents = mutableMapOf<String, Long>()
        val lastEventTimestamp = mutableMapOf<String, Long>()
        val event = UsageEvents.Event()
        while (usageEventsIterator.hasNextEvent()) {
            usageEventsIterator.getNextEvent(event)
            val eventName = event.packageName
            val eventType = event.eventType
            val eventTimestamp = event.timeStamp

            if (eventType == UsageEvents.Event.ACTIVITY_PAUSED || eventType == UsageEvents.Event.ACTIVITY_STOPPED) {
                lastEventTimestamp[eventName]?.let { lastTimeStamp ->
                    val usage = usageEvents[eventName] ?: 0
                    usageEvents[eventName] = usage + (eventTimestamp - lastTimeStamp)
                }
            } else if (eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                lastEventTimestamp[eventName] = eventTimestamp
            }
        }
        return usageEvents
    }


 */
