package com.progetto.stats.backend.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class CalculatedStatsDB(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val query = ("CREATE TABLE " + TABLE_NAME + " (" +
                ID_COL + " INTEGER PRIMARY KEY, " +
                APP_NAME_COL + " TEXT," +
                TOTAL_TIME_IN_FOREGROUND_COL + " INTEGER," +
                TOTAL_TIME_IN_BACKGROUND_COL + " INTEGER," +
                LAST_TIME_STAMP_COL + " INTEGER," +
                PACKAGE_NAME_COL + " TEXT" +
                ")")
        db.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }


    fun addUsageStats(appName: String, totalTimeInForeground: Long, totalTimeInBackground: Long, lastTimeStamp: Long, packageName: String) {
        val db = this.writableDatabase
        db.use { db ->
            val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $APP_NAME_COL = ?", arrayOf(appName))
            cursor.use { cursor ->
                val values = ContentValues().apply {
                    put(APP_NAME_COL, appName)
                    put(TOTAL_TIME_IN_FOREGROUND_COL, totalTimeInForeground)
                    put(TOTAL_TIME_IN_BACKGROUND_COL, totalTimeInBackground)
                    put(LAST_TIME_STAMP_COL, lastTimeStamp)
                    put(PACKAGE_NAME_COL, packageName)
                }
                if (cursor.moveToFirst()) {
                    db.update(TABLE_NAME, values, "$APP_NAME_COL = ?", arrayOf(appName))
                } else {
                    db.insert(TABLE_NAME, null, values)
                }
            }
        }
    }



    fun getAllUsageStats(): List<StatDataClass<String, Long, Long, Long,String>> {
        val usageStatsList = mutableListOf<StatDataClass<String, Long, Long, Long,String>>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, null)

        cursor.use { cur ->
            if (cur.moveToFirst()) {
                do {
                    val appNameIndex = cur.getColumnIndex(APP_NAME_COL)
                    val totalTimeInForegroundIndex = cur.getColumnIndex(TOTAL_TIME_IN_FOREGROUND_COL)
                    val totalTimeInBackgroundIndex = cur.getColumnIndex(TOTAL_TIME_IN_BACKGROUND_COL)
                    val lastTimeStampIndex = cur.getColumnIndex(LAST_TIME_STAMP_COL)
                    val packageNameIndex = cur.getColumnIndex(PACKAGE_NAME_COL)

                    val appName = if (appNameIndex != -1) cur.getString(appNameIndex) else ""
                    val packageName = if (packageNameIndex != -1) cur.getString(packageNameIndex) else ""
                    val totalTimeInForeground = if (totalTimeInForegroundIndex != -1) cur.getLong(totalTimeInForegroundIndex) else 0L
                    val totalTimeInBackground = if (totalTimeInBackgroundIndex != -1) cur.getLong(totalTimeInBackgroundIndex) else 0L
                    val lastTimeStamp = if (lastTimeStampIndex != -1) cur.getLong(lastTimeStampIndex) else 0L

                    // Aggiungi solo se la somma dei tempi in foreground e in background è maggiore di 0L
                    if (totalTimeInForeground + totalTimeInBackground > 0L) {
                        val usageStats = StatDataClass(appName, totalTimeInForeground, totalTimeInBackground, lastTimeStamp,packageName)
                        usageStatsList.add(usageStats)
                    }
                } while (cur.moveToNext())
            }
        }

        db.close()
        if (usageStatsList.isEmpty()) return emptyList() else return usageStatsList
    }

    fun deleteUsageStats(appName: String): Int {
        val db = this.writableDatabase

        val rowsAffected = db.delete(TABLE_NAME, "$APP_NAME_COL = ?", arrayOf(appName))
        db.close()
        return rowsAffected
    }

    fun deleteAllRows(): Int {
        val db = this.writableDatabase
        val rowsAffected = db.delete(TABLE_NAME, null, null)
        db.close()
        return rowsAffected
    }

    companion object {
        private val DATABASE_NAME = "db"
        private val DATABASE_VERSION = 1
        private val TABLE_NAME = "calculated_stats_table"
        private val ID_COL = "id"
        private val APP_NAME_COL = "app_name"
        private val PACKAGE_NAME_COL = "package_name"
        private val LAST_TIME_STAMP_COL = "last_time_stamp"
        private val TOTAL_TIME_IN_FOREGROUND_COL = "total_time_in_foreground"
        private val TOTAL_TIME_IN_BACKGROUND_COL = "total_time_in_background"
    }
}