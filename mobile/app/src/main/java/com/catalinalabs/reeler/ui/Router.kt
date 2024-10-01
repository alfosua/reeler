package com.catalinalabs.reeler.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.catalinalabs.reeler.ui.components.VideoPlayer
import com.catalinalabs.reeler.ui.screens.DownloaderScreen
import com.catalinalabs.reeler.ui.screens.HistoryScreen
import com.catalinalabs.reeler.ui.screens.PremiumScreen

@Composable
fun Router(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("Preview!")
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = Routes.Home.name,
        modifier = modifier,
    ) {
        composable(route = Routes.Home.name) {
            DownloaderScreen(
                viewModel = hiltViewModel(),
                modifier = Modifier.fillMaxHeight(),
            )
        }
        composable(route = Routes.Downloads.name) {
            HistoryScreen(
                viewModel = hiltViewModel(),
                modifier = Modifier.fillMaxHeight(),
            )
        }
        composable(route = Routes.Premium.name) {
            PremiumScreen()
        }
        composable(route = Routes.VideoPlayer.name) {
            VideoPlayer()
        }
    }
}
