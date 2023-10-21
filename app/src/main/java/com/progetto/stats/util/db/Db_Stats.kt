package com.progetto.stats.util.db

data class Db_Stats<A, B, C, D,E >(
    val appName: A,
    val totalTimeInForeground: B,
    val totalTimeInBackground: C,
    val lastTimeStamp: D,
    val packageName:E
)