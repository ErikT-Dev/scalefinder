package com.eriktrummal.scalefinder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun BackgroundImage(
    backgroundImageRes: Int,
    backgroundTint: Color,
    backgroundOpacity: Float,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(backgroundImageRes)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(backgroundOpacity),
            contentScale = ContentScale.Crop
        )
        // Tint Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundTint.copy(alpha = backgroundOpacity))
        )
        // Content
        content()
    }
}