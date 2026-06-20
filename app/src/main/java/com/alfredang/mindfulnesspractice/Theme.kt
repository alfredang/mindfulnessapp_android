package com.alfredang.mindfulnesspractice

import androidx.compose.ui.graphics.Color

/** Central color palette (dark teal) — mirrors the iOS app's Theme.swift. */
object Theme {
    val header = Color(red = 0.18f, green = 0.48f, blue = 0.60f)
    val background = Color(red = 0.04f, green = 0.14f, blue = 0.18f)
    val surface = Color.White.copy(alpha = 0.10f)
    val ink = Color.White
    val mutedInk = Color.White.copy(alpha = 0.72f)
    val control = Color.White
    val progress = Color.White.copy(alpha = 0.95f)
    val progressTrack = Color.White.copy(alpha = 0.14f)
}
