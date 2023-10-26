package com.progetto.stats.backend

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.progetto.stats.backend.task.AddToDBWorker
import java.util.concurrent.TimeUnit

class Helper(val context: Context) {
     val INTERVALLOCAMPIONAMENTO:Long =15

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
    

    fun provaScikitLearn(){
        val python = Python.getInstance()
        val pythonFile = python.getModule("prova")
        val calcolaErroreMedioAssoluto = pythonFile.callAttr("calcola_errore_medio_assoluto") as PyObject
        val erroreMedio = calcolaErroreMedioAssoluto.toString().toDouble()
        Log.d("debug_tag", "$erroreMedio")
    }


    fun schedulePeriodicAddToDBWorker() {
        val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .build()

        val addToDBWorkRequest = PeriodicWorkRequestBuilder<AddToDBWorker>(INTERVALLOCAMPIONAMENTO, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork("addToDBWorker", ExistingPeriodicWorkPolicy.KEEP, addToDBWorkRequest)
    }

    fun startPython(){
        if( !Python.isStarted() ) {
            Python.start( AndroidPlatform( context ) )
        }
    }

}