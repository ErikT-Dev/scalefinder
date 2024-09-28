package com.eriktrummal.scalefinder.ui.bars

import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.eriktrummal.scalefinder.R
import com.eriktrummal.scalefinder.Screen
import com.eriktrummal.scalefinder.screensInBottom

@Composable
fun BottomBar(navController: NavController, onTitleChange: (String) -> Unit) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val systemBarsInsets = WindowInsets.systemBars.asPaddingValues()

    Box(
        Modifier
            .fillMaxWidth()
            .padding(bottom = systemBarsInsets.calculateBottomPadding())
    ) {
        BottomNavigation(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            backgroundColor = colorResource(id = R.color.bottom_bar_fill_color_1),
            contentColor = colorResource(id = R.color.bottom_bar_fill_color_2)
        ) {
            screensInBottom.forEach { item ->
                val isSelected = when (currentRoute) {
                    item.bRoute -> true
                    Screen.SelectedMode.route, Screen.SelectedScale.route -> item.bRoute == Screen.BottomScreen.AllScales.bRoute
                    else -> currentRoute == item.bRoute
                }
                val tint = if (isSelected) colorResource(id = R.color.bottom_bar_selected_item)
                else colorResource(id = R.color.bottom_bar_unselected_item)

                BottomNavigationItem(
                    selected = isSelected,
                    onClick = {
                        navController.navigate(item.bRoute) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                        onTitleChange(item.bTitle)
                    },
                    icon = {
                        Icon(
                            painter = painterResource(id = item.icon),
                            contentDescription = item.bTitle,
                            tint = tint
                        )
                    },
                    label = { Text(text = item.bTitle, color = tint) },
                    selectedContentColor = colorResource(id = R.color.bottom_bar_selected_item),
                    unselectedContentColor = colorResource(id = R.color.bottom_bar_unselected_item)
                )
            }
        }
    }
}