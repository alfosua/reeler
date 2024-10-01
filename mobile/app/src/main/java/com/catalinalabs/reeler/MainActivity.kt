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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.catalinalabs.reeler.ui.components.AdBanner
import com.catalinalabs.reeler.ui.components.AdBannerSize
import com.catalinalabs.reeler.ui.components.VideoPlayer
import com.catalinalabs.reeler.ui.screens.DownloaderScreen
import com.catalinalabs.reeler.ui.screens.HistoryScreen
import com.catalinalabs.reeler.ui.theme.ReelerTheme
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MobileAds.initialize(this)
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(listOf("ABCDEF012345"))
                .build()
        )

        enableEdgeToEdge()
        setContent {
            ReelerTheme {
                ReelerApp()
            }
        }
    }
}

data class NavBarItem(
    val title: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean,
    val badgeCount: Int? = null,
)

@Composable
fun ReelerApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppBar()
        },
        bottomBar = {
            NavBar(navController)
        },
    ) { innerPadding ->
        Router(
            navController = navController,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(modifier: Modifier = Modifier) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
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
    )
}

@Composable
fun NavBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val navBarItems = listOf(
        NavBarItem(
            title = stringResource(R.string.home),
            route = Routes.Home.name,
            selectedIcon = Icons.Rounded.Home,
            unselectedIcon = Icons.Outlined.Home,
            hasNews = false,
        ),
        NavBarItem(
            title = stringResource(R.string.downloads),
            route = Routes.Downloads.name,
            selectedIcon = Icons.Rounded.Download,
            unselectedIcon = Icons.Outlined.Download,
            hasNews = false,
        ),
        NavBarItem(
            title = stringResource(R.string.premium),
            route = Routes.Premium.name,
            selectedIcon = Icons.Filled.WorkspacePremium,
            unselectedIcon = Icons.Outlined.WorkspacePremium,
            hasNews = false,
        ),
    )
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    val handleClick = { index: Int, item: NavBarItem ->
        selectedItem = index
        navController.navigate(item.route)
    }

    Column(modifier) {
        Box(
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            AdBanner(
                size = AdBannerSize.Large,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        NavigationBar {
            navBarItems.forEachIndexed { index, item ->
                NavigationBarItem(
                    selected = selectedItem == index,
                    onClick = { handleClick(index, item) },
                    label = { Text(item.title) },
                    icon = { NavBarIcon(item, selectedItem, index) }
                )
            }
        }
    }
}

@Composable
fun NavBarIcon(
    item: NavBarItem,
    selectedItem: Int,
    index: Int,
) {
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

@Composable
fun Router(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Home.name,
        modifier = modifier,
    ) {
        composable(route = Routes.Home.name) {
            DownloaderScreen(
                viewModel = hiltViewModel(),
                modifier = Modifier.fillMaxHeight(),
            )
        }
        composable(route = Routes.Downloads.name) {
            HistoryScreen(
                viewModel = hiltViewModel(),
                modifier = Modifier.fillMaxHeight(),
            )
        }
        composable(route = Routes.Premium.name) {
            ComingSoon()
        }
        composable(route = Routes.VideoPlayer.name) {
            VideoPlayer()
        }
    }
}

enum class Routes {
    Home,
    Downloads,
    Premium,
    VideoPlayer,
}

@Composable
fun ComingSoon() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "Coming soon!")
    }
}

@Preview(showBackground = true)
@Composable
fun ReelerAppPreview() {
    ReelerTheme {
        ReelerApp()
    }
}

@Preview(showBackground = true)
@Composable
fun ReelerAppDarkThemePreview() {
    ReelerTheme(darkTheme = true) {
        ReelerApp()
    }
}
