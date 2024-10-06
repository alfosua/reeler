package com.catalinalabs.reeler.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FileDownloadDone
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.catalinalabs.reeler.R
import com.catalinalabs.reeler.data.DownloadEntity
import com.catalinalabs.reeler.data.DownloadMockData
import com.catalinalabs.reeler.ui.components.DownloadItem
import com.catalinalabs.reeler.ui.models.DownloadActionsViewModel
import com.catalinalabs.reeler.ui.models.DownloadProcessStatus
import com.catalinalabs.reeler.ui.models.DownloaderViewModel
import com.catalinalabs.reeler.ui.theme.ReelerTheme

@Composable
fun DownloaderScreen(
    viewModel: DownloaderViewModel,
    actions: DownloadActionsViewModel,
    modifier: Modifier = Modifier,
    navigateToVideoPlayer: (String) -> Unit = { },
) {
    val context = LocalContext.current
    val download = viewModel.download

    DownloaderScreen(
        status = viewModel.status,
        download = viewModel.download,
        sourceUrl = viewModel.sourceUrl,
        onVideoUrlChange = viewModel::updateSourceUrl,
        startDownloadProcess = {
            viewModel.startDownloadProcess(context)
        },
        alreadyHandledSendAction = viewModel.alreadyHandledSendAction,
        markAsAlreadyHandledSendAction = viewModel::markAsAlreadyHandleSendAction,
        navigateToVideoPlayer = navigateToVideoPlayer,
        modifier = modifier,
        onOpenOn = {
            if (download != null) {
                actions.openItemOnSocialMedia(context, download)
            }
        },
        onShare = {
            if (download != null) {
                actions.shareItem(context, download)
            }
        },
        onDelete = {
            if (download != null) {
                actions.deleteItem(download)
                viewModel.updateDownload(null)
                viewModel.updateStatus(DownloadProcessStatus.Idle)
            }
        },
    )
}

@Composable
fun DownloaderScreen(
    sourceUrl: String,
    status: DownloadProcessStatus,
    download: DownloadEntity?,
    onVideoUrlChange: (String) -> Unit,
    startDownloadProcess: () -> Unit,
    modifier: Modifier = Modifier,
    alreadyHandledSendAction: Boolean = false,
    navigateToVideoPlayer: (String) -> Unit = { },
    onOpenOn: () -> Unit = { },
    onDelete: () -> Unit = { },
    onShare: () -> Unit = { },
    markAsAlreadyHandledSendAction: () -> Unit = { },
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (context is Activity && !alreadyHandledSendAction
            && context.intent?.action == Intent.ACTION_SEND
        ) {
            val urlFromIntent = context.intent.extras?.getString(Intent.EXTRA_TEXT)
            if (urlFromIntent != null) {
                onVideoUrlChange(urlFromIntent)
                startDownloadProcess()
                markAsAlreadyHandledSendAction()
            }
        }
    }

    Column(modifier) {
        DownloadField(
            videoUrl = sourceUrl,
            onVideoUrlChange = onVideoUrlChange,
            onDownloadButtonClick = startDownloadProcess,
            modifier = Modifier.fillMaxWidth(),
        )
        if (download != null) {
            Text(
                text = "Most recent download",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp, start = 16.dp, top = 16.dp),
            )
            DownloadItem(
                download = download,
                onItemClick = {
                    if (download.filePath != null) {
                        navigateToVideoPlayer(download.filePath)
                    }
                },
                onOpenOn = onOpenOn,
                onShare = onShare,
                onDelete = onDelete,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        DownloadProcessStatusTracker(
            status,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DownloaderScreenPreview() {
    ReelerTheme {
        DownloaderScreen(
            sourceUrl = "",
            status = DownloadProcessStatus.DownloadSuccess,
            download = DownloadMockData.forPreview[0],
            onVideoUrlChange = { },
            startDownloadProcess = { },
            modifier = Modifier.fillMaxHeight(),
        )
    }
}

@Composable
fun DownloadField(
    videoUrl: String,
    onVideoUrlChange: (String) -> Unit,
    onDownloadButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.padding(8.dp)) {
        OutlinedTextField(
            value = videoUrl,
            onValueChange = onVideoUrlChange,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 0.dp,
                bottomStart = 16.dp,
                bottomEnd = 0.dp
            ),
            label = {
                Text(stringResource(R.string.paste_video_link_here))
            },
            modifier = Modifier.weight(1f),
        )
        Button(
            onClick = onDownloadButtonClick,
            shape = RoundedCornerShape(
                topStart = 0.dp,
                topEnd = 16.dp,
                bottomStart = 0.dp,
                bottomEnd = 16.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
            modifier = Modifier
                .padding(top = 8.dp)
                .height(56.dp),
        ) {
            Text(
                text = stringResource(R.string.download),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DownloadFieldPreview() {
    ReelerTheme {
        DownloadField(
            videoUrl = "",
            onVideoUrlChange = { },
            onDownloadButtonClick = { },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun DownloadProcessStatusTracker(
    status: DownloadProcessStatus,
    modifier: Modifier = Modifier,
    successContent: @Composable () -> Unit = { },
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (status) {
            is DownloadProcessStatus.Processing -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                )
                Text(
                    text = stringResource(R.string.processing_video_information),
                )
            }

            is DownloadProcessStatus.ProcessingSuccess -> {
                Icon(
                    imageVector = Icons.Rounded.VideoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                )
                Text(
                    text = stringResource(R.string.your_video_was_processed_successfully),
                )
                successContent()
            }

            is DownloadProcessStatus.Downloading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                )
                Text(
                    text = stringResource(R.string.download_in_progress),
                )
            }

            is DownloadProcessStatus.DownloadSuccess -> {
                Icon(
                    imageVector = Icons.Rounded.FileDownloadDone,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                )
                Text(text = stringResource(R.string.video_downloaded_successfully))
            }

            is DownloadProcessStatus.Error -> {
                Icon(
                    imageVector = Icons.Rounded.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                )
                Text(
                    text = stringResource(R.string.something_went_wrong),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DownloadProcessStatusTrackerPreviewWhenProcessing() {
    ReelerTheme {
        DownloadProcessStatusTracker(
            status = DownloadProcessStatus.Processing,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DownloadProcessStatusTrackerPreviewWhenProcessingSuccess() {
    ReelerTheme {
        DownloadProcessStatusTracker(
            status = DownloadProcessStatus.ProcessingSuccess,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DownloadProcessStatusTrackerPreviewWhenDownloading() {
    ReelerTheme {
        DownloadProcessStatusTracker(
            status = DownloadProcessStatus.Downloading,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DownloadProcessStatusTrackerPreviewWhenDownloadSuccess() {
    ReelerTheme {
        DownloadProcessStatusTracker(
            status = DownloadProcessStatus.DownloadSuccess,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DownloadProcessStatusTrackerPreviewWhenError() {
    ReelerTheme {
        DownloadProcessStatusTracker(
            status = DownloadProcessStatus.Error("Connection error"),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
