package com.catalinalabs.reeler.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.catalinalabs.reeler.R
import com.catalinalabs.reeler.ui.theme.ReelerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(modifier: Modifier = Modifier) {
    val isLight = MaterialTheme.colorScheme.background.luminance() > 0.5
    CenterAlignedTopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor =
                if (isLight) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.primaryContainer,
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter =
                        if (isLight) painterResource(R.drawable.reeler_color_icon)
                        else painterResource(R.drawable.reeler_light_icon),
                    contentDescription = null,
                    modifier = Modifier.width(24.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Reeler",
                    style = MaterialTheme.typography.titleLarge,
                    color =
                        if (isLight) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
fun TopAppBarPreview() {
    ReelerTheme {
        TopAppBar()
    }
}

@Preview(showBackground = true)
@Composable
fun TopAppBarPreviewWithDarkTheme() {
    ReelerTheme(darkTheme = true) {
        TopAppBar()
    }
}
