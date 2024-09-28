package com.eriktrummal.scalefinder

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.eriktrummal.scalefinder.pages.AllScales
import com.eriktrummal.scalefinder.pages.MyScales
import com.eriktrummal.scalefinder.pages.ScaleFinder
import com.eriktrummal.scalefinder.ui.views.SelectedScaleView
import com.eriktrummal.scalefinder.ui.views.SelectedModeView

@Composable
fun Navigation(
    navController: NavController,
    viewModel: MainViewModel
) {
    NavHost(
        navController = navController as NavHostController,
        startDestination = Screen.BottomScreen.ScaleFinder.bRoute,
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 56.dp)
    ) {
        composable(Screen.BottomScreen.ScaleFinder.bRoute) {
            ScaleFinder(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(Screen.BottomScreen.MyScales.bRoute) {
            MyScales(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(Screen.BottomScreen.AllScales.bRoute) {
            AllScales(navController, viewModel)
        }
        composable(
            route = Screen.SelectedMode.route,
            arguments = listOf(
                navArgument("familyName") { type = NavType.StringType },
                navArgument("modeName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val familyName = backStackEntry.arguments?.getString("familyName") ?: ""
            val modeName = backStackEntry.arguments?.getString("modeName") ?: ""
            SelectedModeView(
                family = familyName,
                mode = modeName,
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(
            route = Screen.SelectedScale.route,
            arguments = listOf(navArgument("scaleId") { type = NavType.IntType })
        ) { backStackEntry ->
            val scaleId = backStackEntry.arguments?.getInt("scaleId") ?: -1
            SelectedScaleView(
                scaleId = scaleId,
                viewModel = viewModel,
                navController = navController
            )
        }
    }
}