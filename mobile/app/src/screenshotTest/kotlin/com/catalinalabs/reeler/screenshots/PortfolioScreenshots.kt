package com.catalinalabs.reeler.screenshots

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.catalinalabs.reeler.data.live.DownloadStatus
import com.catalinalabs.reeler.data.testing.DownloadMockData
import com.catalinalabs.reeler.ui.TopAppBar
import com.catalinalabs.reeler.ui.models.PremiumAuthState
import com.catalinalabs.reeler.ui.screens.DownloaderScreen
import com.catalinalabs.reeler.ui.screens.HistoryScreen
import com.catalinalabs.reeler.ui.screens.PremiumScreenContent
import com.catalinalabs.reeler.ui.theme.ReelerTheme

private const val PHONE = "spec:width=1080px,height=2280px,dpi=440"

@Composable
private fun PhoneFrame(
    selectedTab: Int,
    darkTheme: Boolean = false,
    content: @Composable (Modifier) -> Unit,
) {
    ReelerTheme(darkTheme = darkTheme) {
        Surface {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = { TopAppBar() },
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { },
                            label = { Text("Home") },
                            icon = {
                                Icon(
                                    if (selectedTab == 0) Icons.Rounded.Home
                                    else Icons.Outlined.Home,
                                    contentDescription = null,
                                )
                            },
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { },
                            label = { Text("Downloads") },
                            icon = {
                                Icon(
                                    if (selectedTab == 1) Icons.Rounded.Download
                                    else Icons.Outlined.Download,
                                    contentDescription = null,
                                )
                            },
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { },
                            label = { Text("Premium") },
                            icon = {
                                Icon(
                                    if (selectedTab == 2) Icons.Filled.WorkspacePremium
                                    else Icons.Outlined.WorkspacePremium,
                                    contentDescription = null,
                                )
                            },
                        )
                    }
                },
            ) { innerPadding ->
                content(
                    Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                )
            }
        }
    }
}

@PreviewTest
@Preview(device = PHONE, showBackground = true)
@Composable
fun HomeSuccess() {
    PhoneFrame(selectedTab = 0) { modifier ->
        DownloaderScreen(
            sourceUrl = "",
            status = DownloadStatus.Success,
            download = DownloadMockData.forPreview[0],
            onVideoUrlChange = { },
            startDownloadProcess = { },
            modifier = modifier,
        )
    }
}

@PreviewTest
@Preview(device = PHONE, showBackground = true)
@Composable
fun HomeDownloading() {
    PhoneFrame(selectedTab = 0) { modifier ->
        DownloaderScreen(
            sourceUrl = "https://www.instagram.com/reel/C9AbCdEfGhI/",
            status = DownloadStatus.Downloading(64.0, 1, 3),
            download = DownloadMockData.forPreview[1],
            onVideoUrlChange = { },
            startDownloadProcess = { },
            modifier = modifier,
        )
    }
}

@PreviewTest
@Preview(device = PHONE, showBackground = true)
@Composable
fun DownloadsHistory() {
    PhoneFrame(selectedTab = 1) { modifier ->
        HistoryScreen(
            downloads = DownloadMockData.forPreview,
            modifier = modifier,
            onOpenOn = { },
            onShare = { },
            onDelete = { },
        )
    }
}

@PreviewTest
@Preview(device = PHONE, showBackground = true)
@Composable
fun DownloadsHistoryDark() {
    PhoneFrame(selectedTab = 1, darkTheme = true) { modifier ->
        HistoryScreen(
            downloads = DownloadMockData.forPreview,
            modifier = modifier,
            onOpenOn = { },
            onShare = { },
            onDelete = { },
        )
    }
}

@PreviewTest
@Preview(device = PHONE, showBackground = true)
@Composable
fun PremiumSignedOut() {
    PhoneFrame(selectedTab = 2) { modifier ->
        PremiumScreenContent(
            authState = PremiumAuthState.SignedOut,
            isPremium = false,
            username = null,
            email = "creator@example.com",
            password = "correcthorse",
            code = "",
            errorMessage = null,
            isBusy = false,
            modifier = modifier,
        )
    }
}

@PreviewTest
@Preview(device = PHONE, showBackground = true)
@Composable
fun PremiumMember() {
    PhoneFrame(selectedTab = 2, darkTheme = true) { modifier ->
        PremiumScreenContent(
            authState = PremiumAuthState.SignedIn,
            isPremium = true,
            username = "creator@example.com",
            email = "",
            password = "",
            code = "",
            errorMessage = null,
            isBusy = false,
            modifier = modifier,
        )
    }
}
