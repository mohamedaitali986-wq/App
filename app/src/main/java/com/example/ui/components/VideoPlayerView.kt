package com.example.ui.components

import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.SecondaryAccent
import kotlinx.coroutines.delay

@Composable
fun VideoPlayerView(
    videoUrl: String,
    thumbnailUrl: String = "",
    isPlaying: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isVideoPaused by remember { mutableStateOf(!isPlaying) }
    var showPauseOverlay by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }

    LaunchedEffect(isPlaying) {
        isVideoPaused = !isPlaying
        if (isPlaying) {
            videoViewRef?.start()
        } else {
            videoViewRef?.pause()
        }
    }

    DisposableEffect(videoUrl) {
        onDispose {
            videoViewRef?.stopPlayback()
        }
    }

    // Media player progress tick
    LaunchedEffect(isPlaying, isVideoPaused) {
        if (isPlaying && !isVideoPaused) {
            while (true) {
                delay(200)
                val vv = videoViewRef
                if (vv != null && vv.duration > 0) {
                    progress = (vv.currentPosition.toFloat() / vv.duration.toFloat()).coerceIn(0f, 1f)
                } else {
                    progress = (progress + 0.02f) % 1.0f
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isVideoPaused = !isVideoPaused
                showPauseOverlay = true
                if (isVideoPaused) {
                    videoViewRef?.pause()
                } else {
                    videoViewRef?.start()
                }
            }
    ) {
        // Fallback Canvas Background while loading or if video URL is blank
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DarkCanvas,
                        Color(0xFF1E1335),
                        Color(0xFF2A102A),
                        DarkCanvas
                    )
                )
            )

            val waveRadius = (canvasWidth * 0.4f) + (progress * 80f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        PrimaryAccent.copy(alpha = 0.35f),
                        SecondaryAccent.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(canvasWidth * 0.5f, canvasHeight * 0.45f),
                    radius = waveRadius
                ),
                center = Offset(canvasWidth * 0.5f, canvasHeight * 0.45f),
                radius = waveRadius
            )
        }

        // Actual Video Player using Android Native VideoView
        val isRealUrl = videoUrl.isNotBlank() && (
            videoUrl.startsWith("http://") ||
            videoUrl.startsWith("https://") ||
            videoUrl.startsWith("content://") ||
            videoUrl.startsWith("file://") ||
            videoUrl.startsWith("/")
        )

        if (isRealUrl) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        setOnPreparedListener { mp ->
                            mp.isLooping = true
                            if (isPlaying && !isVideoPaused) {
                                start()
                            }
                        }
                        setOnErrorListener { _, _, _ ->
                            true // Handle gracefully
                        }
                        setVideoURI(Uri.parse(videoUrl))
                        videoViewRef = this
                    }
                },
                update = { vView ->
                    videoViewRef = vView
                    val currentTag = vView.tag as? String
                    if (currentTag != videoUrl) {
                        vView.tag = videoUrl
                        vView.setVideoURI(Uri.parse(videoUrl))
                    }
                    if (isPlaying && !isVideoPaused) {
                        if (!vView.isPlaying) {
                            vView.start()
                        }
                    } else {
                        if (vView.isPlaying) {
                            vView.pause()
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Tap Play/Pause Feedback Overlay
        LaunchedEffect(showPauseOverlay) {
            if (showPauseOverlay) {
                delay(800)
                showPauseOverlay = false
            }
        }

        AnimatedVisibility(
            visible = showPauseOverlay && isVideoPaused,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "تشغيل الفيديو",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        // Bottom Video Scrub Progress Bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .align(Alignment.BottomCenter),
            color = PrimaryAccent,
            trackColor = Color.White.copy(alpha = 0.2f)
        )
    }
}
