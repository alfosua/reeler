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
import androidx.navigation.toRoute
import com.catalinalabs.reeler.data.schema.DownloadLog
import com.catalinalabs.reeler.ui.components.MediaViewer
import com.catalinalabs.reeler.ui.screens.DownloaderScreen
import com.catalinalabs.reeler.ui.screens.HistoryScreen
import com.catalinalabs.reeler.ui.screens.PremiumScreen

fun NavHostController.navigateToMediaViewer(download: DownloadLog) {
    val file = download.info?.file
        ?: download.info?.items?.firstOrNull()?.child?.file
    val filePath = file?.filePath ?: return
    navigate(Routes.MediaViewer(filePath, file.contentType))
}

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
        startDestination = Routes.Home,
        modifier = modifier,
    ) {
        composable<Routes.Home> {
            DownloaderScreen(
                viewModel = hiltViewModel(),
                actions = hiltViewModel(),
                modifier = Modifier.fillMaxHeight(),
                navigateToMediaViewer = { download ->
                    navController.navigateToMediaViewer(download)
                },
            )
        }
        composable<Routes.Downloads> {
            HistoryScreen(
                viewModel = hiltViewModel(),
                downloadActionsViewModel = hiltViewModel(),
                modifier = Modifier.fillMaxHeight(),
                navigateToMediaViewer = { download ->
                    navController.navigateToMediaViewer(download)
                },
            )
        }
        composable<Routes.Premium> {
            PremiumScreen(
                viewModel = hiltViewModel(),
            )
        }
        composable<Routes.MediaViewer> { backStackEntry ->
            val route: Routes.MediaViewer = backStackEntry.toRoute()
            MediaViewer(
                filePath = route.filePath,
                contentType = route.contentType,
            )
        }
    }
}
