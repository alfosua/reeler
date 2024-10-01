package com.catalinalabs.reeler.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.catalinalabs.reeler.ui.theme.ReelerTheme

@Composable
fun ReelerApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar()
        },
        bottomBar = {
            BottomNavBar(navController)
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
