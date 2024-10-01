package com.catalinalabs.reeler.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.catalinalabs.reeler.R
import com.catalinalabs.reeler.data.DownloadEntity
import com.catalinalabs.reeler.data.DownloadMockData
import com.catalinalabs.reeler.ui.components.AdBanner
import com.catalinalabs.reeler.ui.components.AdBannerSize
import com.catalinalabs.reeler.ui.models.HistoryViewModel
import com.catalinalabs.reeler.ui.theme.ReelerTheme
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier,
    navigateToVideoPlayer: (String) -> Unit = { },
) {
    val uiState by viewModel.uiState.collectAsState()
    val downloads = uiState.items

    HistoryScreen(
        downloads,
        modifier,
        navigateToVideoPlayer,
    )
}

@Composable
fun HistoryScreen(
    downloads: List<DownloadEntity>,
    modifier: Modifier = Modifier,
    navigateToVideoPlayer: (String) -> Unit = { },
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            )
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
        )
    }
}

@Composable
fun DownloadItem(
    download: DownloadEntity,
    modifier: Modifier = Modifier,
    onItemClick: () -> Unit = { },
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
                .height(72.dp)
                .clickable(onClick = onItemClick),
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(download.thumbnailUrl)
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
                    text = download.duration?.let {
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
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onItemClick),
        ) {
            Text(
                text = download.caption ?: download.filename,
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
                            .data(download.userAvatarUrl)
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
                    text = "@${download.username}",
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
fun DownloadItemPreview() {
    ReelerTheme {
        DownloadItem(
            download = DownloadMockData.forPreview[0],
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
