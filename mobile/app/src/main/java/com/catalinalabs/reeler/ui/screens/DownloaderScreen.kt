package com.catalinalabs.reeler.ui.screens

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
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
import com.catalinalabs.reeler.network.models.VideoInfoOutput
import com.catalinalabs.reeler.ui.models.DownloadProcessStatus
import com.catalinalabs.reeler.ui.models.DownloaderViewModel
import com.catalinalabs.reeler.ui.theme.ReelerTheme
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.Locale

@Composable
fun DownloaderScreen(
    viewModel: DownloaderViewModel,
    modifier: Modifier = Modifier,
    saveVideoToMediaStore: (ContentResolver, ByteArray, VideoInfoOutput) -> String = ::saveVideoToMediaStore,
    showInterstitialAd: (Context) -> Unit = ::showInterstitialAd,
) {
    DownloaderScreen(
        status = viewModel.status,
        videoInfo = viewModel.videoInfo,
        sourceUrl = viewModel.sourceUrl,
        processVideoInfo = viewModel::processVideoInfo,
        processDownload = viewModel::processDownload,
        setVideoUrl = viewModel::setVideoUrl,
        saveVideoToMediaStore = saveVideoToMediaStore,
        showInterstitialAd = showInterstitialAd,
        modifier = modifier,
    )
}

@Composable
fun DownloaderScreen(
    sourceUrl: String,
    status: DownloadProcessStatus,
    videoInfo: VideoInfoOutput?,
    processVideoInfo: () -> Unit,
    processDownload: ((ByteArray, VideoInfoOutput) -> String) -> Unit,
    setVideoUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
    saveVideoToMediaStore: (ContentResolver, ByteArray, VideoInfoOutput) -> String = ::saveVideoToMediaStore,
    showInterstitialAd: (Context) -> Unit = ::showInterstitialAd,
) {
    val context = LocalContext.current
    val resolver = context.contentResolver
    val processVideoInfoAndShowInterstitial = {
        processVideoInfo()
        showInterstitialAd(context)
    }

    LaunchedEffect(Unit) {
        if (context is Activity && context.intent?.action == Intent.ACTION_SEND) {
            val urlFromIntent = context.intent.extras?.getString(Intent.EXTRA_TEXT)
            if (urlFromIntent != null) {
                setVideoUrl(urlFromIntent)
                processVideoInfoAndShowInterstitial()
            }
        }
    }

    if (status is DownloadProcessStatus.ProcessingSuccess) {
        LaunchedEffect(Unit) {
            processDownload { data, videoInfo ->
                saveVideoToMediaStore(resolver, data, videoInfo)
            }
        }
    }

    Column(modifier) {
        DownloadField(
            videoUrl = sourceUrl,
            onVideoUrlChange = setVideoUrl,
            onDownloadButtonClick = processVideoInfo,
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
            setVideoUrl = { },
            processDownload = { },
            saveVideoToMediaStore = { _,_,_ -> "" },
            showInterstitialAd = { },
            processVideoInfo = { },
            sourceUrl = "https://instagram.com/",
            status = DownloadProcessStatus.DownloadSuccess,
            modifier = Modifier.fillMaxHeight(),
            videoInfo = VideoInfoOutput(
                filename = "ig-downloader-1727597487.mp4",
                contentUrl = "https://instagram.alfosuag.workers.dev/video-download/aHR0cHM6Ly9zY29udGVudC15eXoxLTEuY2RuaW5zdGFncmFtLmNvbS9vMS92L3QxNi9mMS9tODIvRUY0MjE3NTkyQ0IyMDBEM0U2QTAzMDIyMzM0M0E5QUNfdmlkZW9fZGFzaGluaXQubXA0P3N0cD1kc3QtbXA0JmVmZz1leUp4WlY5bmNtOTFjSE1pT2lKYlhDSnBaMTkzWldKZlpHVnNhWFpsY25sZmRuUnpYMjkwWmx3aVhTSXNJblpsYm1OdlpHVmZkR0ZuSWpvaWRuUnpYM1p2WkY5MWNteG5aVzR1WTJ4cGNITXVZekl1TnpJd0xtSmhjMlZzYVc1bEluMCZfbmNfY2F0PTExMCZ2cz04MTA3NTA1OTA5MTQ0ODRfMjYzMDQyMzQ1OCZfbmNfdnM9SEJrc0ZRSVlUMmxuWDNod2RsOXlaV1ZzYzE5d1pYSnRZVzVsYm5SZmNISnZaQzlGUmpReU1UYzFPVEpEUWpJd01FUXpSVFpCTURNd01qSXpNelF6UVRsQlExOTJhV1JsYjE5a1lYTm9hVzVwZEM1dGNEUVZBQUxJQVFBVkFoZzZjR0Z6YzNSb2NtOTFaMmhmWlhabGNuTjBiM0psTDBkSExYSldhSEl0YTNCeFVYZ3lhMDVCUjFOd2ExVlJSR2hYU1ZsaWNWOUZRVUZCUmhVQ0FzZ0JBQ2dBR0FBYkFCVUFBQ2FXblBHUHhlT0ZRUlVDS0FKRE15d1hRRE16TXpNek16TVlFbVJoYzJoZlltRnpaV3hwYm1WZk1WOTJNUkVBZGY0SEFBJTNEJTNEJl9uY19yaWQ9YjI2MGVjMGE4MyZjY2I9OS00Jm9oPTAwX0FZRGo1eVV5MGZHdThrMm4zbmhVQlVLMTBoS3MxcGFrZkJkM2thSG1nRHJUdkEmb2U9NjZGQUVEMjQmX25jX3NpZD0xMGQxM2I=.mp4",
                width = 750,
                height = 1333,
                username = "cris_villegas07",
                caption = "Part 1 | Jujutsu Kaisen 222-235 manga chapters Animation 🔥",
                duration = 19.2,
                userAvatarUrl = "https://scontent-yyz1-1.cdninstagram.com/v/t51.2885-19/457621679_877143554292925_4043951588544897022_n.jpg?stp=dst-jpg_e0_s150x150&_nc_ht=scontent-yyz1-1.cdninstagram.com&_nc_cat=101&_nc_ohc=BsuRG3nmJBYQ7kNvgHKLVtG&_nc_gid=b260e280f8ee4b079f61b4c67b4b2c92&edm=APs17CUBAAAA&ccb=7-5&oh=00_AYBkT_2-MZ4SwYe5vD9FCmwdMLvxhJT03wn43vDi9cDFmA&oe=66FEEF3C&_nc_sid=10d13b",
                thumbnailUrl = "https://scontent-yyz1-1.cdninstagram.com/v/t51.29350-15/441015281_460146716487560_3290678782543390784_n.jpg?stp=c0.280.720.720a_dst-jpg_e15_s640x640&_nc_ht=scontent-yyz1-1.cdninstagram.com&_nc_cat=108&_nc_ohc=6VUypcyjz54Q7kNvgHCfifs&_nc_gid=b260e280f8ee4b079f61b4c67b4b2c92&edm=APs17CUBAAAA&ccb=7-5&oh=00_AYBSTuioxdGQnAKVeFPWaHcr9efa18de73IH0RDqQqrmkg&oe=66FEDE70&_nc_sid=10d13b"
            ),
        )
    }
}

private fun saveVideoToMediaStore(
    resolver: ContentResolver,
    data: ByteArray,
    videoInfo: VideoInfoOutput,
): String {
    val targetPath = Environment.DIRECTORY_DOWNLOADS + "/Reeler Videos"
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, videoInfo.filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
        put(MediaStore.MediaColumns.RELATIVE_PATH, targetPath)
    }
    val baseUri = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        else -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    }
    val uri = resolver.insert(baseUri, contentValues)

    Log.d("DownloaderScreen", "Saving file \"${videoInfo.filename}\" to \"$targetPath\" as media ($uri)")

    resolver.openOutputStream(uri!!)?.apply {
        write(data)
        close()
    }

    return uri.toString()
}

private fun showInterstitialAd(context: Context) {
    if (context !is Activity) {
        throw Exception("Context is not an activity")
    }
    InterstitialAd.load(
        context,
        "ca-app-pub-8588662607604944/6439579637",
        AdRequest.Builder().build(),
        object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                super.onAdFailedToLoad(adError)
                TODO("Handle the error.")
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                super.onAdLoaded(interstitialAd)
                interstitialAd.show(context)
            }
        }
    )
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
            videoInfo = VideoInfoOutput(
                filename = "ig-downloader-1727597487.mp4",
                contentUrl = "https://instagram.alfosuag.workers.dev/video-download/aHR0cHM6Ly9zY29udGVudC15eXoxLTEuY2RuaW5zdGFncmFtLmNvbS9vMS92L3QxNi9mMS9tODIvRUY0MjE3NTkyQ0IyMDBEM0U2QTAzMDIyMzM0M0E5QUNfdmlkZW9fZGFzaGluaXQubXA0P3N0cD1kc3QtbXA0JmVmZz1leUp4WlY5bmNtOTFjSE1pT2lKYlhDSnBaMTkzWldKZlpHVnNhWFpsY25sZmRuUnpYMjkwWmx3aVhTSXNJblpsYm1OdlpHVmZkR0ZuSWpvaWRuUnpYM1p2WkY5MWNteG5aVzR1WTJ4cGNITXVZekl1TnpJd0xtSmhjMlZzYVc1bEluMCZfbmNfY2F0PTExMCZ2cz04MTA3NTA1OTA5MTQ0ODRfMjYzMDQyMzQ1OCZfbmNfdnM9SEJrc0ZRSVlUMmxuWDNod2RsOXlaV1ZzYzE5d1pYSnRZVzVsYm5SZmNISnZaQzlGUmpReU1UYzFPVEpEUWpJd01FUXpSVFpCTURNd01qSXpNelF6UVRsQlExOTJhV1JsYjE5a1lYTm9hVzVwZEM1dGNEUVZBQUxJQVFBVkFoZzZjR0Z6YzNSb2NtOTFaMmhmWlhabGNuTjBiM0psTDBkSExYSldhSEl0YTNCeFVYZ3lhMDVCUjFOd2ExVlJSR2hYU1ZsaWNWOUZRVUZCUmhVQ0FzZ0JBQ2dBR0FBYkFCVUFBQ2FXblBHUHhlT0ZRUlVDS0FKRE15d1hRRE16TXpNek16TVlFbVJoYzJoZlltRnpaV3hwYm1WZk1WOTJNUkVBZGY0SEFBJTNEJTNEJl9uY19yaWQ9YjI2MGVjMGE4MyZjY2I9OS00Jm9oPTAwX0FZRGo1eVV5MGZHdThrMm4zbmhVQlVLMTBoS3MxcGFrZkJkM2thSG1nRHJUdkEmb2U9NjZGQUVEMjQmX25jX3NpZD0xMGQxM2I=.mp4",
                width = 750,
                height = 1333,
                username = "cris_villegas07",
                caption = "Part 1 | Jujutsu Kaisen 222-235 manga chapters Animation 🔥",
                duration = 19.2,
                userAvatarUrl = "https://scontent-yyz1-1.cdninstagram.com/v/t51.2885-19/457621679_877143554292925_4043951588544897022_n.jpg?stp=dst-jpg_e0_s150x150&_nc_ht=scontent-yyz1-1.cdninstagram.com&_nc_cat=101&_nc_ohc=BsuRG3nmJBYQ7kNvgHKLVtG&_nc_gid=b260e280f8ee4b079f61b4c67b4b2c92&edm=APs17CUBAAAA&ccb=7-5&oh=00_AYBkT_2-MZ4SwYe5vD9FCmwdMLvxhJT03wn43vDi9cDFmA&oe=66FEEF3C&_nc_sid=10d13b",
                thumbnailUrl = "https://scontent-yyz1-1.cdninstagram.com/v/t51.29350-15/441015281_460146716487560_3290678782543390784_n.jpg?stp=c0.280.720.720a_dst-jpg_e15_s640x640&_nc_ht=scontent-yyz1-1.cdninstagram.com&_nc_cat=108&_nc_ohc=6VUypcyjz54Q7kNvgHCfifs&_nc_gid=b260e280f8ee4b079f61b4c67b4b2c92&edm=APs17CUBAAAA&ccb=7-5&oh=00_AYBSTuioxdGQnAKVeFPWaHcr9efa18de73IH0RDqQqrmkg&oe=66FEDE70&_nc_sid=10d13b"
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
