package com.dttrackpro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.dttrackpro.app.ui.navigation.DTTrackNavGraph
import com.dttrackpro.app.ui.theme.DTTrackProTheme
import com.dttrackpro.app.ui.theme.Graphite900

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DTTrackProTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Graphite900) {
                    DTTrackNavGraph()
                }
            }
        }
    }
}
