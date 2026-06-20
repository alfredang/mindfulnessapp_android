package com.alfredang.mindfulnesspractice

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun PracticeScreen(viewModel: PracticeViewModel = viewModel()) {
    val context = LocalContext.current
    var scrubValue by remember { mutableStateOf(0.0) }
    var isScrubbing by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    // Mirror the player's currentTime into the local scrub value unless the user is dragging.
    if (!isScrubbing) scrubValue = viewModel.currentTime

    // Background-music picker: an audio document the user owns on this device.
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, MUSIC_PERMISSION_FLAGS)
            }
            val title = queryDisplayName(context, uri) ?: "Background music"
            viewModel.setBackgroundMusic(title, uri)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Theme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Header()

            Image(
                painter = painterResource(R.drawable.practice_zen),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1180f / 1080f)
                    .clip(androidx.compose.ui.graphics.RectangleShape)
            )

            Spacer(modifier = Modifier.weight(1f))

            Controls(
                viewModel = viewModel,
                scrubValue = scrubValue,
                onScrubChange = { isScrubbing = true; scrubValue = it },
                onScrubFinished = { isScrubbing = false; viewModel.seek(scrubValue) },
                menuExpanded = menuExpanded,
                onMenuExpandedChange = { menuExpanded = it },
                onOpenMusicPicker = {
                    picker.launch(arrayOf("audio/*", "application/ogg"))
                },
                modifier = Modifier
                    .padding(horizontal = 22.dp)
                    .padding(bottom = 24.dp)
                    .navigationBarsPadding()
            )
        }
    }
}

@Composable
private fun Header() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Theme.header)
    ) {
        Text(
            text = "Mindfulness Practice",
            color = Theme.ink,
            fontSize = 33.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 2,
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(top = 22.dp, bottom = 24.dp)
        )
    }
}

@Composable
private fun Controls(
    viewModel: PracticeViewModel,
    scrubValue: Double,
    onScrubChange: (Double) -> Unit,
    onScrubFinished: () -> Unit,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    onOpenMusicPicker: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        OptionsRow(
            viewModel = viewModel,
            menuExpanded = menuExpanded,
            onMenuExpandedChange = onMenuExpandedChange,
            onOpenMusicPicker = onOpenMusicPicker,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = timeString(viewModel.currentTime),
                color = Theme.ink,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(58.dp)
            )

            Slider(
                value = scrubValue.toFloat(),
                onValueChange = { onScrubChange(it.toDouble()) },
                onValueChangeFinished = onScrubFinished,
                valueRange = 0f..max(viewModel.sessionLength, 1.0).toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = Theme.progress,
                    activeTrackColor = Theme.progress,
                    inactiveTrackColor = Theme.progressTrack,
                ),
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "-" + timeString(max(viewModel.sessionLength - viewModel.currentTime, 0.0)),
                color = Theme.ink,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                modifier = Modifier.width(68.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterHorizontally)
        ) {
            ControlButton("Start", Icons.Filled.PlayArrow) { viewModel.start() }
            ControlButton("Pause", Icons.Filled.Pause, dimmed = !viewModel.isPlaying) { viewModel.pause() }
            ControlButton("Stop", Icons.Filled.Stop) { viewModel.stop() }
        }
    }
}

@Composable
private fun OptionsRow(
    viewModel: PracticeViewModel,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    onOpenMusicPicker: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                Pill(
                    icon = Icons.Filled.Timer,
                    text = "${(viewModel.sessionLength / 60).toInt()} min",
                    onClick = { onMenuExpandedChange(true) },
                )
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { onMenuExpandedChange(false) },
                ) {
                    viewModel.lengthOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text("${(option / 60).toInt()} min") },
                            leadingIcon = {
                                if (viewModel.sessionLength == option) {
                                    Icon(Icons.Filled.Check, contentDescription = null)
                                }
                            },
                            onClick = {
                                viewModel.selectSessionLength(option)
                                onMenuExpandedChange(false)
                            }
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                Pill(
                    icon = Icons.Filled.MusicNote,
                    text = viewModel.musicTitle ?: "Background Music",
                    trailingIcon = if (viewModel.musicTitle != null) Icons.Filled.Cancel else null,
                    onClick = {
                        // Tapping the little "x" clears the current track instead of opening the picker.
                        if (viewModel.musicTitle != null) viewModel.clearBackgroundMusic()
                        else onOpenMusicPicker()
                    },
                )
            }
        }

        viewModel.musicError?.let { error ->
            Text(
                text = error,
                color = Theme.mutedInk,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun Pill(
    icon: ImageVector,
    text: String,
    trailingIcon: ImageVector? = null,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterHorizontally),
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(Theme.surface)
            .border(1.dp, Theme.progressTrack, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Theme.ink, modifier = Modifier.size(18.dp))
        Text(text = text, color = Theme.ink, fontSize = 15.sp, maxLines = 1)
        if (trailingIcon != null) {
            Icon(trailingIcon, contentDescription = null, tint = Theme.ink, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun ControlButton(
    title: String,
    icon: ImageVector,
    dimmed: Boolean = false,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp),
        modifier = Modifier
            .widthIn(min = 76.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .alpha(if (dimmed) 0.72f else 1f)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(Theme.surface)
                .border(1.dp, Theme.progressTrack, CircleShape)
        ) {
            Icon(icon, contentDescription = title, tint = Theme.control, modifier = Modifier.size(28.dp))
        }
        Text(text = title, color = Theme.mutedInk, fontSize = 15.sp)
    }
}

private fun timeString(seconds: Double): String {
    val total = max(seconds.toInt(), 0)
    return "${total / 60}:" + (total % 60).toString().padStart(2, '0')
}

private fun queryDisplayName(context: android.content.Context, uri: android.net.Uri): String? {
    return runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) cursor.getString(idx)?.substringBeforeLast('.') else null
                } else null
            }
    }.getOrNull()
}
