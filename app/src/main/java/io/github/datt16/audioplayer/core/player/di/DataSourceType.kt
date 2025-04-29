package io.github.datt16.audioplayer.core.player.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class CacheDataSourceType

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class HttpDataSourceType
