package com.catalinalabs.reeler.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.catalinalabs.reeler.data.schema.DownloadLog
import com.catalinalabs.reeler.data.testing.DownloadMockData
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
    downloads: List<DownloadLog>,
    modifier: Modifier = Modifier,
    navigateToVideoPlayer: (String) -> Unit = { },
    onOpenOn: (DownloadLog) -> Unit,
    onShare: (DownloadLog) -> Unit,
    onDelete: (DownloadLog) -> Unit,
) {
    LazyColumn(modifier) {
        itemsIndexed(downloads) { index, download ->
            val filePath = download.info?.file?.filePath
            DownloadItem(
                download = download,
                onItemClick = {
                    if (filePath != null) {
                        navigateToVideoPlayer(filePath)
                    }
                },
                onOpenOn = { onOpenOn(download) },
                onShare = { onShare(download) },
                onDelete = { onDelete(download) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
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
