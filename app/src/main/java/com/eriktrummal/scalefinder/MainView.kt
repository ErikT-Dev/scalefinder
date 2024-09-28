package com.eriktrummal.scalefinder

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.rememberModalBottomSheetState
import com.eriktrummal.scalefinder.ui.bars.BottomBar
import com.eriktrummal.scalefinder.ui.views.SettingsView
import com.eriktrummal.scalefinder.ui.bars.Sidebar
import com.eriktrummal.scalefinder.ui.bars.TopBar

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainView() {
    val scaffoldState: ScaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val viewModel: MainViewModel = viewModel()
    val controller: NavController = rememberNavController()
    val currentScreen = remember { viewModel.currentScreen.value }
    val title = remember { mutableStateOf(currentScreen.title) }
    val systemBarsInsets = WindowInsets.systemBars.asPaddingValues()

    val isSheetFullScreen by remember { mutableStateOf(false) }
    val modifier = if (isSheetFullScreen) Modifier.fillMaxSize() else Modifier.fillMaxWidth()
    val modalSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmValueChange = { it != ModalBottomSheetValue.HalfExpanded }
    )
    val roundedCornerRadius = if (isSheetFullScreen) 0.dp else 12.dp

    ModalBottomSheetLayout(
        sheetState = modalSheetState,
        sheetShape = RoundedCornerShape(topStart = roundedCornerRadius, topEnd = roundedCornerRadius),
        sheetContent = {
            SettingsView(modifier = modifier)
        }
    ) {
        Scaffold(
            bottomBar = {
                BottomBar(
                    navController = controller,
                    onTitleChange = { newTitle -> title.value = newTitle }
                )
            },
            topBar = {
                TopBar(
                    title = title.value,
                    scaffoldState = scaffoldState,
                    scope = scope,
                    onSettingsClick = {
                        scope.launch {
                            if (modalSheetState.isVisible) modalSheetState.hide()
                            else modalSheetState.show()
                        }
                    }
                )
            },
            scaffoldState = scaffoldState,
            drawerContent = {
                Sidebar(viewModel = viewModel)
            },
            modifier = Modifier.padding(top = systemBarsInsets.calculateTopPadding())
        ) { innerPadding ->
            Navigation(
                navController = controller,
                viewModel = viewModel
            )
        }
    }
}