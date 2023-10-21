package com.progetto.stats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.progetto.stats.ui.pages.MainBody
import com.progetto.stats.util.Helper
import com.progetto.stats.util.db.CalculatedDBStats
import com.progetto.stats.util.db.Db_Stats


class MainActivity : ComponentActivity() {
    val calculatedDBStats = CalculatedDBStats(this, null)
    var usageStatsList=mutableListOf<Db_Stats<String, Long, Long, Long,String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainBody(this).scaffold()
        }
        if( !Python.isStarted() ) {
            Python.start( AndroidPlatform( this ) )
        }
        Helper(this).schedulePeriodicAddToDBWorker()
        usageStatsList= calculatedDBStats.getAllUsageStats().toMutableList()

    }

    override fun onResume(){
        super.onResume()
        if(Helper(this).permissionCheck()){
            setContent {
                MainBody(this).scaffold()
            }
        }
    }




}
