package com.alfredang.mindfulnesspractice

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            PracticeScreen()
        }
    }
}

/** Persistable read flags for the document the user picks as background music. */
const val MUSIC_PERMISSION_FLAGS = Intent.FLAG_GRANT_READ_URI_PERMISSION
