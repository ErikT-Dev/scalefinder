package com.eriktrummal.scalefinder.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eriktrummal.scalefinder.MainViewModel
import com.eriktrummal.scalefinder.R
import com.eriktrummal.scalefinder.data.ScaleItem
import com.eriktrummal.scalefinder.ui.components.BackgroundImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectedModeView(family: String, mode: String, viewModel: MainViewModel, navController: NavController) {
    val scales by viewModel.scales.collectAsState()

    LaunchedEffect(family, mode) {
        viewModel.loadScalesForFamilyAndMode(family, mode)
        viewModel.setExpandedFamily(family)
    }

    BackgroundImage(
        backgroundImageRes = R.drawable.bg1,
        backgroundTint = colorResource(id = R.color.background_image_tint),
        backgroundOpacity = 0.6f
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Back", style = MaterialTheme.typography.titleMedium, color = Color.LightGray) },
                    modifier = Modifier.height(50.dp),
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back to All Scales"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                items(scales) { scale ->
                    ScaleItem(scale, navController, viewModel)
                }
            }
        }
    }
}