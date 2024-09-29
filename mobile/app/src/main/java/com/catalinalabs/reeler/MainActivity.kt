package com.catalinalabs.reeler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SlowMotionVideo
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.catalinalabs.reeler.ui.screens.DownloaderScreen
import com.catalinalabs.reeler.ui.theme.ReelerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReelerTheme {
                ReelerApp()
            }
        }
    }
}

data class NavBarItem (
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean,
    val badgeCount: Int? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReelerApp() {
    val navBarItems = listOf(
        NavBarItem(
            title = "Home",
            selectedIcon = Icons.Rounded.Home,
            unselectedIcon = Icons.Outlined.Home,
            hasNews = false,
        ),
        NavBarItem(
            title = "Downloads",
            selectedIcon = Icons.Rounded.Download,
            unselectedIcon = Icons.Outlined.Download,
            hasNews = false,
        ),
        NavBarItem(
            title = "Premium",
            selectedIcon = Icons.Filled.WorkspacePremium,
            unselectedIcon = Icons.Outlined.WorkspacePremium,
            hasNews = false,
        ),
    )
    var selectedItem by rememberSaveable {
        mutableIntStateOf(0)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SlowMotionVideo,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Reeler",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                )
            )
        },
        bottomBar = {
            Column {
                Box(
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Text(
                        text = "AdMob Banner Here!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(8.dp),
                    )
                }
                NavigationBar {
                    navBarItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = selectedItem == index,
                            onClick = { selectedItem = index },
                            label = {
                                Text(item.title)
                            },
                            icon = {
                                BadgedBox(
                                    badge ={
                                        if (item.badgeCount != null) {
                                            Badge {
                                                Text(item.badgeCount.toString())
                                            }
                                        } else if (item.hasNews) {
                                            Badge()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector =
                                        if (selectedItem == index) item.selectedIcon
                                        else item.unselectedIcon,
                                        contentDescription = item.title,
                                    )
                                }
                            }
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        DownloaderScreen(
            viewModel = viewModel(),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxHeight(),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReelerAppPreview() {
    ReelerTheme {
        ReelerApp()
    }
}
