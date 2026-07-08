package com.catalinalabs.reeler.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.catalinalabs.reeler.R
import java.io.File

/**
 * Full view of a downloaded file: videos get the ExoPlayer view, images are
 * rendered with Coil.
 */
@Composable
fun MediaViewer(
    filePath: String,
    contentType: String?,
    modifier: Modifier = Modifier,
) {
    if (contentType?.startsWith("image/") == true) {
        val context = LocalContext.current
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(File(filePath))
                    .crossfade(true)
                    .build(),
                loading = {
                    CircularProgressIndicator()
                },
                contentDescription = stringResource(R.string.video_thumbnail),
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    } else {
        VideoPlayer(filePath = filePath)
    }
}
