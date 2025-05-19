package io.github.datt16.audioplayer.screens.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.datt16.audioplayer.core.designsystem.AudioPlayerAppTheme
import io.github.datt16.audioplayer.core.navigation.AudioPlayerAppDestinations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerAppScaffold(
  navController: NavController,
  modifier: Modifier = Modifier,
  content: @Composable (paddingValues: PaddingValues) -> Unit,
) {
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentDestination = navBackStackEntry?.destination

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = {
          Text("Audio Player")
        }
      )
    },
    bottomBar = {
      NavigationBar {
        NavigationBarItem(
          icon = {
            Icon(
              imageVector = Icons.Default.Home,
              contentDescription = "Home"
            )
          },
          label = { Text("Home") },
          selected = currentDestination?.hierarchy?.any { it.hasRoute<AudioPlayerAppDestinations.Home>() } == true,
          onClick = {
            navController.navigate(AudioPlayerAppDestinations.Home) {
              popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
              }
              launchSingleTop = true
              restoreState = true
            }
          }
        )
        NavigationBarItem(
          icon = {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "Setting"
            )
          },
          label = { Text("Settings") },
          selected = currentDestination?.hierarchy?.any { it.hasRoute<AudioPlayerAppDestinations.GlobalSettings>() } == true,
          onClick = {
            navController.navigate(AudioPlayerAppDestinations.GlobalSettings) {
              popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
              }
              launchSingleTop = true
              restoreState = true
            }
          }
        )
        NavigationBarItem(
          icon = {
            Icon(
              imageVector = Icons.Default.Create,
              contentDescription = null
            )
          },
          label = { Text("HandsOn") },
          selected = currentDestination?.hierarchy?.any { it.hasRoute<AudioPlayerAppDestinations.HandsOnTop>() } == true,
          onClick = {
            navController.navigate(AudioPlayerAppDestinations.HandsOnTop) {
              popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
              }
              launchSingleTop = true
              restoreState = true
            }
          }
        )
      }
    }
  ) {
    content(it)
  }
}

@Preview
@Composable
private fun AudioPlayerAppScaffoldPreview() {
  AudioPlayerAppTheme {
    AudioPlayerAppScaffold(rememberNavController()) {
      Column {
        Text("Hello, This is sample text")
      }
    }
  }
}
