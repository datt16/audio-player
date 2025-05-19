package io.github.datt16.audioplayer.core.navigation

import kotlinx.serialization.Serializable

object AudioPlayerAppDestinations {
  @Serializable
  object Home

  @Serializable
  object GlobalSettings

  @Serializable
  object HandsOnTop
}

@Serializable
abstract class HandsOnTopDestination(
  val title: String,
  val description: String,
)

object HandsOnTopDestinations {
  @Serializable
  object HandsOnTop : HandsOnTopDestination(
    title = "HandsOnTop",
    description = "各種試作画面への遷移"
  )

  @Serializable
  object AudioPlayerManagerPlayground : HandsOnTopDestination(
    title = "AudioPlayerManager Playground",
    description = "AudioPlayerManagerを用いた再生画面のサンプル"
  )
}
