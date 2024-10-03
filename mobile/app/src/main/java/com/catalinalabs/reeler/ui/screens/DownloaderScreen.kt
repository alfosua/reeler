package com.catalinalabs.reeler.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FileDownloadDone
import androidx.compose.material.icons.rounded.PlayArrow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.catalinalabs.reeler.R
import com.catalinalabs.reeler.data.DownloadMockData
import com.catalinalabs.reeler.data.asVideoInfoOutput
import com.catalinalabs.reeler.network.models.VideoInfoOutput
import com.catalinalabs.reeler.ui.models.DownloadProcessStatus
import com.catalinalabs.reeler.ui.models.DownloaderViewModel
import com.catalinalabs.reeler.ui.theme.ReelerTheme
import java.util.Locale

@Composable
fun DownloaderScreen(
    viewModel: DownloaderViewModel,
    modifier: Modifier = Modifier,
    navigateToVideoPlayer: (String) -> Unit = { },
) {
    val context = LocalContext.current

    DownloaderScreen(
        status = viewModel.status,
        videoInfo = viewModel.videoInfo,
        sourceUrl = viewModel.sourceUrl,
        onVideoUrlChange = viewModel::updateSourceUrl,
        startDownloadProcess = {
            viewModel.startDownloadProcess(context)
        },
        navigateToVideoPlayer = navigateToVideoPlayer,
        modifier = modifier,
    )
}

@Composable
fun DownloaderScreen(
    sourceUrl: String,
    status: DownloadProcessStatus,
    videoInfo: VideoInfoOutput?,
    onVideoUrlChange: (String) -> Unit,
    startDownloadProcess: () -> Unit,
    modifier: Modifier = Modifier,
    navigateToVideoPlayer: (String) -> Unit = { },
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (context is Activity && context.intent?.action == Intent.ACTION_SEND) {
            val urlFromIntent = context.intent.extras?.getString(Intent.EXTRA_TEXT)
            if (urlFromIntent != null) {
                onVideoUrlChange(urlFromIntent)
                startDownloadProcess()
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
        if (videoInfo != null) {
            Text(
                text = "Most recent download",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp, start = 16.dp, top = 16.dp),
            )
            VideoItem(
                videoInfo = videoInfo,
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
            onVideoUrlChange = { },
            startDownloadProcess = { },
            sourceUrl = "",
            status = DownloadProcessStatus.DownloadSuccess,
            modifier = Modifier.fillMaxHeight(),
            videoInfo = DownloadMockData.forPreview[0].asVideoInfoOutput(),
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

@Composable
fun VideoItem(
    videoInfo: VideoInfoOutput,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(8.dp)
            .height(IntrinsicSize.Min)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
                .width(128.dp)
                .height(72.dp),
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(videoInfo.thumbnailUrl)
                    .crossfade(true)
                    .build(),
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(modifier = Modifier.size(32.dp)) {
                            CircularProgressIndicator()
                        }
                    }
                },
                contentDescription = stringResource(R.string.video_thumbnail),
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .width(128.dp)
                    .height(72.dp),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 4.dp, bottom = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = videoInfo.duration?.let {
                        val minutes = it.toInt() / 60
                        val seconds = it.toInt() % 60
                        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                    } ?: "00:00",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = videoInfo.caption ?: videoInfo.filename,
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
            )
            Spacer(modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
                        .size(24.dp)
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(videoInfo.userAvatarUrl)
                            .crossfade(true)
                            .build(),
                        loading = {
                            CircularProgressIndicator()
                        },
                        contentDescription = stringResource(R.string.video_thumbnail),
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "@${videoInfo.username}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = stringResource(R.string.more_actions),
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight(),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VideoItemPreview() {
    ReelerTheme {
        VideoItem(
            videoInfo = DownloadMockData.forPreview[0].asVideoInfoOutput(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
