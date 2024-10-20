package com.catalinalabs.reeler.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.catalinalabs.reeler.R
import com.catalinalabs.reeler.data.schema.DownloadLog
import com.catalinalabs.reeler.data.testing.DownloadMockData
import com.catalinalabs.reeler.ui.models.DownloadActionsViewModel
import com.catalinalabs.reeler.ui.theme.ReelerTheme
import java.util.Locale

@Composable
fun DownloadItem(
    download: DownloadLog,
    viewModel: DownloadActionsViewModel,
    modifier: Modifier = Modifier,
    onItemClick: () -> Unit = { },
) {
    val context = LocalContext.current
    DownloadItem(
        download = download,
        modifier = modifier,
        onItemClick = onItemClick,
        onOpenOn = {
            viewModel.openItemOnSocialMedia(context, download)
        },
        onShare = {
            viewModel.shareItem(context, download)
        },
        onDelete = {
            viewModel.deleteItem(download)
        },
    )
}

@Composable
fun DownloadItem(
    download: DownloadLog,
    modifier: Modifier = Modifier,
    onItemClick: () -> Unit = { },
    onOpenOn: () -> Unit = { },
    onDelete: () -> Unit = { },
    onShare: () -> Unit = { },
) {
    Box(modifier.clickable(onClick = onItemClick)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier
                .padding(8.dp)
                .height(IntrinsicSize.Min)
        ) {
            VideoThumbnail(download)
            VideoDescription(download, Modifier.weight(1f))
            ActionsContextMenu(
                onOpenOn = onOpenOn,
                onDelete = onDelete,
                onShare = onShare,
            )
        }
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

@Composable
fun ActionsContextMenu(
    onOpenOn: () -> Unit = { },
    onDelete: () -> Unit = { },
    onShare: () -> Unit = { },
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val handleDropdownItem = { action: () -> Unit ->
        isMenuExpanded = false
        action()
    }

    IconButton(
        onClick = { isMenuExpanded = true },
    ) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = stringResource(R.string.more_actions),
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight(),
        )
        DropdownMenu(
            expanded = isMenuExpanded,
            onDismissRequest = { isMenuExpanded = false }
        ) {
            DropdownMenuItem(
                text = {
                    Text("Open on...")
                },
                onClick = {
                    handleDropdownItem {
                        onOpenOn()
                    }
                }
            )
            DropdownMenuItem(
                text = {
                    Text("Share")
                },
                onClick = {
                    handleDropdownItem {
                        onShare()
                    }
                }
            )
            DropdownMenuItem(
                text = {
                    Text("Delete")
                },
                onClick = {
                    handleDropdownItem {
                        onDelete()
                    }
                }
            )
        }
    }
}

@Composable
fun VideoThumbnail(
    download: DownloadLog,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
            .width(128.dp)
            .height(72.dp),
    ) {
        VideoThumbnailImage(download)
        VideoThumbnailOverlay(download, Modifier.align(Alignment.BottomStart))
    }
}

@Composable
fun VideoThumbnailOverlay(
    download: DownloadLog,
    modifier: Modifier = Modifier,
) {
    val timeText = download.info?.duration?.let {
        val minutes = it.toInt() / 60
        val seconds = it.toInt() % 60
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    } ?: "00:00"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(start = 4.dp, bottom = 4.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.PlayArrow,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.padding(end = 4.dp)
        )
        Text(
            text = timeText,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
fun VideoThumbnailImage(
    download: DownloadLog,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current) {
        Image(
            painter = painterResource(R.drawable.landscape_arnisee_region),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
        return
    }

    val context = LocalContext.current

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(download.info?.thumbnailUrl)
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
        modifier = modifier
            .width(128.dp)
            .height(72.dp),
    )
}

@Composable
fun VideoDescription(
    download: DownloadLog,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxHeight(),
    ) {
        Text(
            text = download.info?.caption ?: download.info?.file?.filename ?: "",
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
        )
        Author(download)
    }
}

@Composable
fun Author(
    download: DownloadLog,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
                .size(24.dp)
        ) {
            AuthorImage(download)
        }
        Spacer(Modifier.width(4.dp))
        Text(
            text = download.info?.author?.name ?: "",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
        )
    }
}

@Composable
fun AuthorImage(
    download: DownloadLog,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current) {
        Image(
            painter = painterResource(R.drawable.user_avatar),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.size(24.dp),
        )
        return
    }

    val context = LocalContext.current

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(download.info?.author?.avatarUrl)
            .crossfade(true)
            .build(),
        loading = {
            CircularProgressIndicator()
        },
        contentDescription = stringResource(R.string.video_thumbnail),
        contentScale = ContentScale.FillWidth,
        modifier = modifier.size(24.dp),
    )
}
