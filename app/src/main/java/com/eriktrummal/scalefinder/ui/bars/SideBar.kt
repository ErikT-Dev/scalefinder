package com.eriktrummal.scalefinder.ui.bars

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eriktrummal.scalefinder.MainViewModel
import com.eriktrummal.scalefinder.R
import com.eriktrummal.scalefinder.ui.components.BackgroundImage

@Composable
fun Sidebar(viewModel: MainViewModel) {
    val families by viewModel.families.collectAsState()
    val familyInclusionState by viewModel.familyInclusionState.collectAsState()

    BackgroundImage(
        backgroundImageRes = R.drawable.bg3,
        backgroundTint = colorResource(id = R.color.sidebar_background_image_tint),
        backgroundOpacity = 0.7f
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 56.dp)
        ) {
            Text(
                text = "Choose which scales to include in the app:",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                fontSize = 19.sp,
                color = colorResource(id = R.color.sidebar_text),
                modifier = Modifier.padding(25.dp)
            )

            val sortedFamilies = families.sortedWith(compareBy(
                { !(familyInclusionState[it.first] ?: true) },
                { families.indexOf(it) }
            ))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(sortedFamilies) { (family, _) ->
                    FamilyButton(
                        family = family,
                        isIncluded = familyInclusionState[family] ?: true,
                        onClick = { viewModel.toggleFamilyInclusion(family) }
                    )
                }
            }
        }
    }
}

@Composable
fun FamilyButton(family: String, isIncluded: Boolean, onClick: () -> Unit) {
    val colorResourceNameStart = "${family.lowercase().replace(" ", "_")}_start"
    val colorResourceNameEnd = "${family.lowercase().replace(" ", "_")}_end"

    val startColor = colorResource(id = getColorResourceId(colorResourceNameStart))
    val endColor = colorResource(id = getColorResourceId(colorResourceNameEnd))

    val gradient = Brush.horizontalGradient(
        colors = listOf(
            startColor.copy(alpha = if (isIncluded) 0.8f else 0.4f),
            endColor.copy(alpha = if (isIncluded) 0.8f else 0.4f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(
                RoundedCornerShape(
                    topStart = 2.dp,
                    topEnd = 16.dp,
                    bottomStart = 2.dp,
                    bottomEnd = 2.dp
                )
            )
            .background(gradient)
            .clickable(onClick = onClick)
    ) {
        Text(
            text = family,
            style = MaterialTheme.typography.labelLarge,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black.copy(alpha = if (isIncluded) 1f else 0.6f),
            modifier = Modifier.padding(16.dp)
        )
    }
}

fun getColorResourceId(name: String): Int {
    return R.color::class.java.getField(name).getInt(null)
}