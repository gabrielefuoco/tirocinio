package com.progetto.stats

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.progetto.stats.ui.pages.MainBody
import com.progetto.stats.util.Helper


class MainActivity : ComponentActivity() {
    val helper=Helper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainBody(this).scaffold()
        }


        helper.startPython()

        helper.schedulePeriodicAddToDBWorker()


    }

    //se esiste un'istanza dell'attività  in cima all'attuale task
    // andorid invierà i dati dell'intent tramite questa fun
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        helper.schedulePeriodicAddToDBWorker()
    }

    override fun onResume(){
        super.onResume()
        if(helper.permissionCheck()){
            setContent {
                MainBody(this).scaffold()
            }
        }
    }




}
