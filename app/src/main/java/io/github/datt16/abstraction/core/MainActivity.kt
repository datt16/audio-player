package io.github.datt16.abstraction.core

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import io.github.datt16.abstraction.core.designsystem.AbstractionAppTheme
import io.github.datt16.abstraction.core.navigation.AbstractionAppNavHost
import io.github.datt16.abstraction.screens.common.AbstractionAppScaffold

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      AbstractionAppTheme {
        AbstractionAppScaffold { paddingValues ->
          AbstractionAppNavHost(modifier = Modifier.padding(paddingValues))
        }
      }
    }
  }
}
