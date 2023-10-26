package com.progetto.stats.ui.pages

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.progetto.stats.ui.theme.ProgettoTheme
import com.progetto.stats.util.BatteryData
import com.progetto.stats.util.Helper
import com.progetto.stats.util.db.CalculatedDBStats
import com.progetto.stats.util.db.Db_Stats
import java.util.Date

class MainBody(val context: Context) {
    val packageManager: PackageManager = context.getPackageManager()
    val calculatedDBStats = CalculatedDBStats(context, null)

    @Composable
    fun body(loginPage : Boolean = false){
        if(!Helper(context).permissionCheck()||loginPage){
            FirstLogin.firstLoginPage(context = context)}
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !loginPage) {
            StatsList()
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun scaffold(){
        var darkTheme by rememberSaveable { mutableStateOf(false) }
        var loginPage by rememberSaveable { mutableStateOf(false)}
        ProgettoTheme(darkTheme = darkTheme){
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Scaffold(
                    topBar = {
                        TopBar(onClick = {
                            loginPage = !loginPage
                        },
                            onClick2 = {darkTheme = !darkTheme},
                            darkTheme=darkTheme
                        )}

                ) {it->
                    Column {
                        Spacer(modifier = Modifier.padding(vertical = (it.calculateTopPadding()/3)*2 ))
                        body(loginPage=loginPage)
                    }
                }
            }
        }
    }

    @Composable
    fun TopBar(onClick: () -> Unit, onClick2: () -> Unit , darkTheme: Boolean=false) {
        Card(
            shape = RoundedCornerShape(1.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 15.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.tertiary)
                    .fillMaxWidth()
                    .height(50.dp),
            ) {
                IconButton(onClick = onClick, modifier = Modifier)
                {
                    Text(text = "⚙️")
                }
                IconButton(onClick = onClick2, modifier = Modifier)
                {
                   if(!darkTheme)Text(text = "\uD83C\uDF19") else Text(text = "☀️")
                     }
            }
        }
    }



    @Composable
    fun batteriaCard() {
        val batteryData = BatteryData
        val batteryPercentage = batteryData.getBatteryPercentage(context)
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        val paddingBattery = screenWidth * batteryPercentage / 100
        val isCharging = batteryData.isCharging(context)

        fun getBackgroundColor(background:Boolean): Color {
            return when (batteryPercentage) {
                in 0..19 -> if(background) Color(0xFF991F00) else Color(0xFFDB3500)
                in 20..35 -> if(background) Color(0xFFE29903) else Color(0xFFFFD947)
                else ->if(background) Color(0xFF00973A) else Color(0xFF59F174)
            }
        }

        Box(modifier = Modifier.padding(bottom = 8.dp, top = 8.dp, start = 15.dp, end = 15.dp)) {
            Card(
                shape = RoundedCornerShape(15.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 15.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .height(140.dp),
                colors = CardDefaults.cardColors(containerColor = getBackgroundColor(false))
            ) {
                Card(
                    Modifier
                        .fillMaxSize()
                        .padding(start = paddingBattery),
                    colors = CardDefaults.cardColors(containerColor = getBackgroundColor(true)),
                    shape = RoundedCornerShape(1.dp)
                ) {
                }
            }

            //DA MODIFICARE COI VALORI CALCOLATI
            Column (modifier= Modifier
                .padding(12.dp)
                .align(Alignment.BottomStart), ){
                Row( verticalAlignment = Alignment.Bottom) {
                    Text(text ="8", color = Color.White,fontSize = 54.sp, modifier = Modifier.alignBy(
                        FirstBaseline
                    ) )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text ="ore" ,color = Color.White,fontSize = 18.sp ,modifier = Modifier.alignBy(
                        FirstBaseline
                    ))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text ="56" ,color = Color.White,fontSize = 54.sp , modifier = Modifier.alignBy(
                        FirstBaseline
                    ))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text ="minuti rimanenti" ,color = Color.White,fontSize = 18.sp , modifier = Modifier.alignBy(
                        FirstBaseline
                    ))
                }
                Spacer(modifier = Modifier.weight(2f))
                Row() {
                    Text(text = "$batteryPercentage% ", color = Color.White,fontSize = 36.sp, modifier = Modifier.alignBy(
                        FirstBaseline
                    ))
                    Spacer(modifier = Modifier.width(4.dp))
                    if(isCharging) Text(text = "| In carica | ", color = Color.White,fontSize = 18.sp,modifier = Modifier.alignBy(
                        FirstBaseline
                    ) )

                }
            }
        }
    }


    @Composable
    private fun expandButton(
        expanded: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        IconButton(
            modifier = modifier.padding(8.dp),
            onClick = onClick
        ) {
            Icon(
                if(!expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary)
        }
    }

    @Composable
    fun appIcon(bitmap: Bitmap, modifier: Modifier = Modifier) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            modifier = modifier
                .size(64.dp)
                .padding(8.dp)
                .clip(RoundedCornerShape(50)),
            contentScale = ContentScale.Crop,
            contentDescription = "App Icon",
        )

    }

    @SuppressLint("NewApi")
    @Composable
    fun CreateStatsCard(
        appName: String,
        icon: Drawable?,
        foregroundTimeInMillis: Long,
        backgroundTimeInMillis: Long,
        lastTimeStamp: Long,
        expanded: Boolean,
        onExpandButtonClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        val foregroundTimeInMinutes = foregroundTimeInMillis / (1000 * 60)
        val foregroundTimeInSeconds = (foregroundTimeInMillis / 1000) % 60
        val backgroundTimeinInMinutes = backgroundTimeInMillis / (1000 * 60)
        val backgroundTimeInSeconds = (backgroundTimeInMillis / 1000) % 60

        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = modifier.padding(bottom = 8.dp, top = 8.dp, start = 15.dp, end = 15.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 10.dp
            )
        ) {
            Column(
                modifier = modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .fillMaxWidth()
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
            ) {
                Row {
                    appIcon(bitmap = icon?.toBitmap() ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
                    Text(
                        text = appName,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(16.dp)
                            .weight(99999f),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(Modifier.weight(1f))
                    expandButton(expanded = expanded, onClick = onExpandButtonClick)
                }
                if (expanded) {
                    if(foregroundTimeInMillis!=0L){
                        Text(
                            text = "Tempo in primo piano: $foregroundTimeInMinutes minuti e $foregroundTimeInSeconds secondi",
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (backgroundTimeInMillis != 0L) {
                        Text(
                            text = "Tempo in background: $backgroundTimeinInMinutes minuti e $backgroundTimeInSeconds secondi",
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Text(
                        text = "Last reading: ${Date(lastTimeStamp)}",
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }


    @Composable
    fun StatsCard(stats: Db_Stats<String, Long, Long, Long, String>){
        var expanded by rememberSaveable { mutableStateOf(false) }
        CreateStatsCard(
            appName = stats.appName,
            icon = try {
                packageManager.getApplicationIcon(stats.packageName)
            } catch (e: PackageManager.NameNotFoundException) {
                packageManager.getDefaultActivityIcon()
            },
            foregroundTimeInMillis = stats.totalTimeInForeground,
            backgroundTimeInMillis = stats.totalTimeInBackground,
            lastTimeStamp = stats.lastTimeStamp,
            expanded = expanded,
            onExpandButtonClick = {
                expanded=!expanded
            }
        )
    }

    @SuppressLint("NewApi")
    @Composable
    fun StatsList(
        modifier: Modifier = Modifier
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(1.dp),
            modifier = modifier.fillMaxSize()
        )
        {
            item{
                batteriaCard()
            }
            itemsIndexed(calculatedDBStats.getAllUsageStats()) { index, stat ->
                if (stat != null) {
                    StatsCard(stat)
                }
            }
        }
    }
}