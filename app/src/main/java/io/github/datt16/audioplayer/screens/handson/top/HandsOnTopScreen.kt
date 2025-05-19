package io.github.datt16.audioplayer.screens.handson.top

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import io.github.datt16.audioplayer.core.designsystem.AudioPlayerAppTheme
import io.github.datt16.audioplayer.core.navigation.HandsOnTopDestinations

@Composable
fun HandsOnTopActionListScreen(
  modifier: Modifier = Modifier,
  navController: NavHostController = rememberNavController(),
) {
  LazyColumn(modifier = modifier.fillMaxSize()) {
    items(listOf(HandsOnTopDestinations.AudioPlayerManagerPlayground)) {
      HandsOnTopActionListItem(
        title = it.title,
        subtitle = it.description,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = {
          navController.navigate(it)
        }
      )
    }
  }
}

@Composable
private fun HandsOnTopActionListItem(
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {},
) {
  Row(
    modifier = modifier
      .clickable { onClick() }
      .fillMaxWidth()
      .background(color = AudioPlayerAppTheme.colors.surface, shape = RoundedCornerShape(12.dp))
      .padding(16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = Icons.Default.Build,
      contentDescription = null,
      modifier = Modifier.size(32.dp),
    )
    VerticalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = AudioPlayerAppTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = AudioPlayerAppTheme.colors.onSurface,
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = subtitle,
        style = AudioPlayerAppTheme.typography.bodyLarge,
        color = AudioPlayerAppTheme.colors.onSurface,
      )
    }
  }
}
