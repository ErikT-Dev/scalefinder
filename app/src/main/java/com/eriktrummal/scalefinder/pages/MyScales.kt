package com.eriktrummal.scalefinder.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eriktrummal.scalefinder.MainViewModel
import com.eriktrummal.scalefinder.R
import com.eriktrummal.scalefinder.data.ScaleItem
import com.eriktrummal.scalefinder.Screen
import com.eriktrummal.scalefinder.ui.components.BackgroundImage

@Composable
fun MyScales(
    viewModel: MainViewModel,
    navController: NavController
) {
    val myScales by viewModel.myScales.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMyScales()
    }

    BackgroundImage(
        backgroundImageRes = R.drawable.bg2,
        backgroundTint = colorResource(id = R.color.background_image_tint),
        backgroundOpacity = 0.6f
    ) {
        Scaffold(
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (myScales.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tap on the heart buttons to add any scale to your list of favourites!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(myScales) { scale ->
                            ScaleItem(
                                scale = scale,
                                navController = navController,
                                viewModel = viewModel,
                                onItemClick = {
                                    navController.navigate(Screen.SelectedScale.createRoute(scale.id.toString()))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}