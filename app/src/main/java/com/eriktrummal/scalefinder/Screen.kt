package com.eriktrummal.scalefinder

import androidx.annotation.DrawableRes

sealed class Screen(
    val title: String,
    val route: String
) {
    sealed class BottomScreen(
        val bTitle: String,
        val bRoute: String,
        @DrawableRes val icon: Int
    ) : Screen(bTitle, bRoute) {
        object ScaleFinder : BottomScreen("Scale Finder", "scaleFinder", R.drawable.baseline_search_24)
        object AllScales : BottomScreen("All Scales", "allScales", R.drawable.baseline_auto_stories_24)
        object MyScales : BottomScreen("My Scales", "myScales", R.drawable.baseline_auto_awesome_24)
    }

    object SelectedMode : Screen("Selected Mode", "selectedMode/{familyName}/{modeName}") {
        fun createRoute(familyName: String, modeName: String) = "selectedMode/$familyName/$modeName"
    }
    object SelectedScale : Screen("Selected Scale", "selectedScale/{scaleId}") {
        fun createRoute(scaleId: String) = "selectedScale/$scaleId"
    }
}

val screensInBottom = listOf(
    Screen.BottomScreen.ScaleFinder,
    Screen.BottomScreen.AllScales,
    Screen.BottomScreen.MyScales
)