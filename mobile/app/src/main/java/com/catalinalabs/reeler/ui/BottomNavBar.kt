package com.catalinalabs.reeler.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.toRoute
import com.catalinalabs.reeler.R
import com.catalinalabs.reeler.ui.components.AdBanner
import com.catalinalabs.reeler.ui.components.AdBannerSize

@Composable
fun BottomNavBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val currentRoute = navController.currentBackStackEntry?.toRoute<Any>()
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    val handleClick = { index: Int, item: NavBarItem ->
        selectedIndex = index
        navController.navigate(item.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    LaunchedEffect(currentRoute) {
        val selectedItem = navBarItems[selectedIndex]
        if (currentRoute != selectedItem.route) {
            val newSelectedItem = navBarItems.find { it.route == currentRoute }
            if (newSelectedItem != null) {
                selectedIndex = navBarItems.indexOf(newSelectedItem)
            }
        }
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
                    selected = selectedIndex == index,
                    onClick = { handleClick(index, item) },
                    label = { Text(stringResource(item.title)) },
                    icon = { NavBarIcon(item, selectedIndex, index) }
                )
            }
        }
    }
}

@Composable
private fun NavBarIcon(
    item: NavBarItem,
    selectedItem: Int,
    index: Int,
) {
    BadgedBox(
        badge = {
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
            contentDescription = stringResource(item.title),
        )
    }
}

private data class NavBarItem(
    @StringRes val title: Int,
    val route: Any,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean,
    val badgeCount: Int? = null,
)

private val navBarItems = listOf(
    NavBarItem(
        title = R.string.home,
        route = Routes.Home,
        selectedIcon = Icons.Rounded.Home,
        unselectedIcon = Icons.Outlined.Home,
        hasNews = false,
    ),
    NavBarItem(
        title = R.string.downloads,
        route = Routes.Downloads,
        selectedIcon = Icons.Rounded.Download,
        unselectedIcon = Icons.Outlined.Download,
        hasNews = false,
    ),
    NavBarItem(
        title = R.string.premium,
        route = Routes.Premium,
        selectedIcon = Icons.Filled.WorkspacePremium,
        unselectedIcon = Icons.Outlined.WorkspacePremium,
        hasNews = false,
    ),
)
