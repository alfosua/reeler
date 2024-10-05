package com.catalinalabs.reeler.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.catalinalabs.reeler.data.DownloadEntity
import com.catalinalabs.reeler.data.DownloadMockData
import com.catalinalabs.reeler.ui.components.AdBanner
import com.catalinalabs.reeler.ui.components.AdBannerSize
import com.catalinalabs.reeler.ui.components.DownloadItem
import com.catalinalabs.reeler.ui.models.DownloadActionsViewModel
import com.catalinalabs.reeler.ui.models.HistoryViewModel
import com.catalinalabs.reeler.ui.theme.ReelerTheme

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier,
    navigateToVideoPlayer: (String) -> Unit = { },
    downloadActionsViewModel: DownloadActionsViewModel = viewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val downloads = uiState.items

    HistoryScreen(
        downloads = downloads,
        modifier = modifier,
        navigateToVideoPlayer = navigateToVideoPlayer,
        onOpenOn = {
            downloadActionsViewModel.openItemOnSocialMedia(context, it)
        },
        onShare = {
            downloadActionsViewModel.shareItem(context, it)
        },
        onDelete = {
            downloadActionsViewModel.deleteItem(it)
        },
    )
}

@Composable
fun HistoryScreen(
    downloads: List<DownloadEntity>,
    modifier: Modifier = Modifier,
    navigateToVideoPlayer: (String) -> Unit = { },
    onOpenOn: (DownloadEntity) -> Unit,
    onShare: (DownloadEntity) -> Unit,
    onDelete: (DownloadEntity) -> Unit,
) {
    LazyColumn(modifier.padding(top = 32.dp)) {
        itemsIndexed(downloads) { index, download ->
            DownloadItem(
                download = download,
                onItemClick = {
                    if (download.mediaUri != null) {
                        navigateToVideoPlayer(download.mediaUri)
                    }
                },
                onOpenOn = { onOpenOn(download) },
                onShare = { onShare(download) },
                onDelete = { onDelete(download) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            if ((index + 1) % 5 == 0) {
                AdBanner(
                    size = AdBannerSize.Small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    ReelerTheme {
        HistoryScreen(
            downloads = DownloadMockData.forPreview,
            modifier = Modifier.fillMaxHeight(),
            onOpenOn = { },
            onShare = { },
            onDelete = { },
        )
    }
}
