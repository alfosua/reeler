package com.catalinalabs.reeler.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.AdSize as AdMobSize

@Composable
fun AdBanner(
    modifier: Modifier = Modifier,
    size: AdBannerSize = AdBannerSize.Small,
) {
    if (LocalInspectionMode.current) {
        Box(
            modifier = modifier
                .width(size.width)
                .height(size.height)
                .background(Color.Red),
        ) {
            Text(
                text = "AdMob Banner Here!",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Center),
            )
        }
    } else {
        val adSize = size.toAdMobSize()
        AndroidView(
            modifier = modifier
                .width(size.width)
                .height(size.height),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(adSize)
                    adUnitId = "ca-app-pub-8588662607604944/2969677296"
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SmallAdBannerPreview() {
    AdBanner(
        size = AdBannerSize.Small,
    )
}

@Preview(showBackground = true)
@Composable
fun LargeAdBannerPreview() {
    AdBanner(
        size = AdBannerSize.Large,
    )
}

@Preview(showBackground = true)
@Composable
fun FullAdBannerPreview() {
    AdBanner(
        size = AdBannerSize.Full,
    )
}

class AdBannerSize(val width: Dp, val height: Dp) {
    fun toAdMobSize(): AdMobSize {
        return when (this) {
            Small -> AdMobSize.BANNER
            Large -> AdMobSize.LARGE_BANNER
            Full -> AdMobSize.FULL_BANNER
            else -> AdMobSize.BANNER
        }
    }
    companion object {
        val Small = AdBannerSize(320.dp, 50.dp)
        val Large = AdBannerSize(320.dp, 100.dp)
        val Full = AdBannerSize(468.dp, 60.dp)
    }
}
