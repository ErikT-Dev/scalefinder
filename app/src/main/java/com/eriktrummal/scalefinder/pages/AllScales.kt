package com.eriktrummal.scalefinder.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eriktrummal.scalefinder.ui.components.BackgroundImage
import com.eriktrummal.scalefinder.MainViewModel
import com.eriktrummal.scalefinder.helpers.MusicalNotationFormatter
import com.eriktrummal.scalefinder.R
import com.eriktrummal.scalefinder.Screen
import com.eriktrummal.scalefinder.data.getResourceId

@Composable
fun AllScales(navController: NavController, viewModel: MainViewModel) {
    val families by viewModel.families.collectAsState()
    val familyInclusionState by viewModel.familyInclusionState.collectAsState()
    val expandedFamily by viewModel.expandedFamily
    val includedFamilies = families.filter { (family, _) ->
        familyInclusionState[family] ?: true
    }

    BackgroundImage(
        backgroundImageRes = R.drawable.bg1,
        backgroundTint = colorResource(id = R.color.background_image_tint),
        backgroundOpacity = 0.6f
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 56.dp)
        ) {
            items(includedFamilies) { (family, _) ->
                ExpandableFamilyItem(
                    family = family,
                    navController = navController,
                    viewModel = viewModel,
                    isExpanded = family == expandedFamily,
                    onExpandToggle = {
                        viewModel.setExpandedFamily(if (expandedFamily == family) null else family)
                    }
                )
            }
        }
    }
}

@Composable
fun ExpandableFamilyItem(
    family: String,
    navController: NavController,
    viewModel: MainViewModel,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit
) {
    val modes by viewModel.modes.collectAsState()
    val greyLineColor = Color.Gray.copy(alpha = 0.5f)

    val resourceNameStart = "${family.lowercase().replace(" ", "_")}_start"
    val resourceNameEnd = "${family.lowercase().replace(" ", "_")}_end"

    val startColor = colorResource(id = getResourceId(resourceNameStart))
    val endColor = colorResource(id = getResourceId(resourceNameEnd))

    Box(
        modifier = Modifier
            .padding(horizontal = 30.dp, vertical = 6.dp)
            .clip(
                RoundedCornerShape(
                    topStart = 2.dp,
                    topEnd = 16.dp,
                    bottomStart = 2.dp,
                    bottomEnd = 2.dp
                )
            )
            .background(
                Brush.horizontalGradient(
                    colors = listOf(startColor, endColor)
                )
            )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onExpandToggle()
                        if (!isExpanded) {
                            viewModel.loadModesForFamily(family)
                        }
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = family,
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Color.Black
                )
            }
            if (isExpanded) {
                Column(modifier = Modifier.padding(start = 32.dp, top = 8.dp, bottom = 8.dp)) {
                    modes.forEach { (modeNr, modeName) ->
                        val formattedModeName = MusicalNotationFormatter.formatModeName(modeName)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(greyLineColor)
                        )

                        Text(
                            text = "$modeNr. $formattedModeName",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate(
                                        Screen.SelectedMode.createRoute(
                                            family,
                                            modeName
                                        )
                                    )
                                }
                                .padding(vertical = 8.dp),
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}