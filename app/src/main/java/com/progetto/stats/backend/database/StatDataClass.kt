package com.progetto.stats.backend.database

data class StatDataClass<A, B, C, D,E >(
    val appName: A,
    val totalTimeInForeground: B,
    val totalTimeInBackground: C,
    val lastTimeStamp: D,
    val packageName:E
)