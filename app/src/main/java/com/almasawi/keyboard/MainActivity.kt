package com.almasawi.keyboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.almasawi.keyboard.ui.App
import com.almasawi.keyboard.ui.AlMasawiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AlMasawiTheme { App() } }
    }
}
