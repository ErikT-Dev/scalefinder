package com.eriktrummal.scalefinder.data

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eriktrummal.scalefinder.MainViewModel
import com.eriktrummal.scalefinder.R
import com.eriktrummal.scalefinder.Screen
import com.eriktrummal.scalefinder.ui.components.PianoView

@Composable
fun ScaleItem(
    scale: Scale,
    navController: NavController,
    viewModel: MainViewModel,
    onItemClick: () -> Unit = {
        navController.navigate(Screen.SelectedScale.createRoute(scale.id.toString()))
    }
) {
    val myScalesStatus by viewModel.myScalesStatus.collectAsState()
    val isInMyScales = myScalesStatus[scale.id] ?: false

    val resourceNameStart = "${scale.family.lowercase().replace(" ", "_")}_start"
    val resourceNameEnd = "${scale.family.lowercase().replace(" ", "_")}_end"

    val startColor = colorResource(id = getResourceId(resourceNameStart))
    val endColor = colorResource(id = getResourceId(resourceNameEnd))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onItemClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(startColor, endColor)
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 0.dp, top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AdaptiveText(
                    text = scale.formattedFullName,
                    modifier = Modifier.weight(1.5f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                PianoView(
                    selectedNotes = scale.notes,
                    noteNames = scale.noteNames,
                    modifier = Modifier
                        .weight(2f)
                        .height(60.dp),
                    rootNote = scale.root,
                    whiteKeyHeight = 55.dp,
                    hideShadows = false
                )

                IconButton(
                    onClick = {
                        if (isInMyScales) {
                            viewModel.removeFromMyScales(scale)
                        } else {
                            viewModel.addToMyScales(scale)
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(id = if (isInMyScales) R.drawable.baseline_favorite_24 else R.drawable.baseline_favorite_border_24),
                        contentDescription = if (isInMyScales) "Remove from MyScales" else "Add to MyScales",
                        tint = colorResource(id = R.color.heart_icon_button),
                        modifier = Modifier.scale(0.9f)
                    )
                }
            }
        }
    }
}

@Composable
fun AdaptiveText(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 2,
    minFontSize: Float = 10f,
    maxFontSize: Float = 16f
) {
    var fontSize by remember { mutableFloatStateOf(maxFontSize) }
    var readyToDraw by remember { mutableStateOf(false) }

    Text(
        text = text,
        fontSize = fontSize.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black,
        maxLines = maxLines,
        softWrap = true,
        modifier = modifier.drawWithContent {
            if (readyToDraw) drawContent()
        },
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowHeight && fontSize > minFontSize) {
                fontSize = (fontSize - 0.5f).coerceAtLeast(minFontSize)
            } else {
                readyToDraw = true
            }
        }
    )
}

fun getResourceId(name: String): Int {
    return try {
        val res = R.color::class.java
        val field = res.getField(name)
        field.getInt(null)
    } catch (e: Exception) {
        throw IllegalArgumentException("No resource ID found for: $name")
    }
}

