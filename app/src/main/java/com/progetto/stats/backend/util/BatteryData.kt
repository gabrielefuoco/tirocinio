package com.progetto.stats.backend.util

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.activity.ComponentActivity

object BatteryData {

    private fun getBatteryCapacity(context: Context?): Double {
        val mPowerProfile: Any
        var batteryCapacity = 0.0
        val power_profile_class = "com.android.internal.os.PowerProfile"
        try {
            mPowerProfile = Class.forName(power_profile_class)
                .getConstructor(Context::class.java)
                .newInstance(context)
            batteryCapacity = Class
                .forName(power_profile_class)
                .getMethod("getBatteryCapacity")
                .invoke(mPowerProfile) as Double
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return batteryCapacity
    }


    fun isCharging(context: Context): Boolean {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context.registerReceiver(null, ifilter)
        }
        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING
        return isCharging
    }

    fun getBatteryPercentage(context: Context): Int {
        val batteryManager = context.getSystemService(ComponentActivity.BATTERY_SERVICE) as BatteryManager
        val batteryPercentage = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        return batteryPercentage
    }


}