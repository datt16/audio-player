import com.android.sdklib.AndroidVersion.VersionCodes

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.detekt)
  alias(libs.plugins.ksp)
  alias(libs.plugins.hilt)

  kotlin(libs.plugins.kotlin.seriazation.get().pluginId) version libs.plugins.kotlin.seriazation.get().version.requiredVersion
}

android {
  namespace = "io.github.datt16.audioplayer"
  compileSdk = VersionCodes.VANILLA_ICE_CREAM

  defaultConfig {
    applicationId = "io.github.datt16.audioplayer"
    minSdk = VersionCodes.Q
    targetSdk = VersionCodes.VANILLA_ICE_CREAM
    versionCode = 29350001
    versionName = "0.1"
  }

  buildTypes {
    debug {
      applicationIdSuffix = ".debug"
      isDebuggable = true
    }
    release {
      isMinifyEnabled = true
      applicationIdSuffix = ".release"
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions {
    jvmTarget = "17"
  }
  buildFeatures {
    buildConfig = true
  }
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.activity.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.appcompat)
  implementation(libs.material)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.hilt.navigation.compose)
  implementation(libs.kotlinx.serialization)
  implementation(libs.kotlinx.coroutines)

  implementation(libs.dagger.hilt.android)
  testImplementation(libs.dagger.hilt.android.testing)
  ksp(libs.dagger.hilt.compiler)

  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.foundation)
  debugImplementation(libs.androidx.compose.ui.tooling.prview)
  debugImplementation(libs.androidx.compose.ui.tooling)

  implementation(libs.androidx.media3.ui)
  implementation(libs.androidx.media3.ui.compose)
  implementation(libs.androidx.media3.exoplayer.hls)
  implementation(libs.androidx.media3.exoplayer)

  testImplementation(libs.junit)
  implementation(libs.timber)
  androidTestImplementation(libs.androidx.junit)
  detektPlugins(libs.detekt.formatting)
  detektPlugins(libs.detekt.compose.rules)
}

detekt {
  parallel = true
  toolVersion = libs.versions.detekt.get()
  config.setFrom(file("${rootProject.projectDir}/config/detekt/detekt.yml"))
  buildUponDefaultConfig = true
  autoCorrect = true
}
