package com.progetto.stats.util

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.progetto.stats.util.db.AddToDBWorker
import java.util.concurrent.TimeUnit

class Helper(val context: Context) {

    fun permissionCheck():Boolean{
        try {
            val packageManager: PackageManager = context.getPackageManager()
            val applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0)
            val appOpsManager = context.getSystemService(ComponentActivity.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                applicationInfo.uid,
                applicationInfo.packageName
            )
            return mode == AppOpsManager.MODE_ALLOWED
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
    }

    fun provaSK(){
        val python = Python.getInstance()
        val pythonFile = python.getModule("prova")
        val calcolaErroreMedioAssoluto = pythonFile.callAttr("calcola_errore_medio_assoluto") as PyObject
        val erroreMedio = calcolaErroreMedioAssoluto.toString().toDouble()
        Log.d("debug_tag", "$erroreMedio")
    }


    fun schedulePeriodicAddToDBWorker() {
        val addToDBWorkRequest = PeriodicWorkRequestBuilder<AddToDBWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork("addToDBWork", ExistingPeriodicWorkPolicy.KEEP, addToDBWorkRequest)
    }

}