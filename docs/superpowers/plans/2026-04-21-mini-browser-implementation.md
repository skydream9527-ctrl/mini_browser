# MiniBrowser Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build MiniBrowser, an Android minimal browser with GeckoView rendering, smart search across 8 engines, and ExoPlayer video takeover.

**Architecture:** Single-Activity Compose app. GeckoView renders pages via AndroidView interop. Navigation Compose handles Home→Browser routing. DataStore persists engine preference. ExoPlayer takes over on fullscreen video requests from GeckoView.

**Tech Stack:** Kotlin 1.9.22, AGP 8.2.2, Jetpack Compose (BOM 2024.02.00), GeckoView 125.0, Media3 ExoPlayer 1.3.0, Navigation Compose 2.7.7, DataStore Preferences 1.0.0

---

## File Structure

```
mini_browser/
├── .gitignore
├── build.gradle.kts                          # Root build config (AGP + Kotlin plugins)
├── settings.gradle.kts                       # Module + repository config (Maven for GeckoView)
├── gradle.properties                         # JVM args, AndroidX, non-transitive R
├── gradle/
│   ├── wrapper/
│   │   ├── gradle-wrapper.jar
│   │   └── gradle-wrapper.properties         # Gradle 8.5
│   └── libs.versions.toml                    # Version catalog
├── app/
│   ├── build.gradle.kts                      # App module: dependencies, compose config
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/minibrowser/app/
│       │   │   ├── MiniBrowserApp.kt         # Application class, GeckoRuntime init
│       │   │   ├── MainActivity.kt           # Single activity, Compose entry
│       │   │   ├── engine/
│       │   │   │   ├── SearchEngineConfig.kt # SearchEngine data class + built-in list
│       │   │   │   ├── UrlUtil.kt            # URL vs search query detection
│       │   │   │   └── GeckoEngineManager.kt # GeckoRuntime/Session lifecycle
│       │   │   ├── player/
│       │   │   │   └── VideoPlayerManager.kt # ExoPlayer fullscreen takeover
│       │   │   ├── data/
│       │   │   │   └── PreferencesRepository.kt # DataStore for engine preference
│       │   │   └── ui/
│       │   │       ├── theme/
│       │   │       │   ├── Color.kt          # Dark tech color palette
│       │   │       │   ├── Type.kt           # Typography
│       │   │       │   └── Theme.kt          # MiniBrowserTheme
│       │   │       ├── components/
│       │   │       │   ├── SearchBar.kt
│       │   │       │   ├── SearchEngineSelector.kt
│       │   │       │   ├── BrowserView.kt    # GeckoView AndroidView wrapper
│       │   │       │   └── VideoPlayer.kt    # ExoPlayer AndroidView wrapper
│       │   │       ├── screens/
│       │   │       │   ├── HomeScreen.kt
│       │   │       │   └── BrowserScreen.kt
│       │   │       └── navigation/
│       │   │           └── NavGraph.kt       # Navigation routes + graph
│       │   └── res/
│       │       ├── values/
│       │       │   ├── strings.xml
│       │       │   └── themes.xml
│       │       ├── drawable/
│       │       │   └── ic_launcher_foreground.xml
│       │       ├── mipmap-anydpi-v26/
│       │       │   ├── ic_launcher.xml
│       │       │   └── ic_launcher_round.xml
│       │       └── xml/
│       │           └── backup_rules.xml
│       └── test/
│           └── java/com/minibrowser/app/
│               └── engine/
│                   ├── UrlUtilTest.kt
│                   └── SearchEngineConfigTest.kt
├── .github/
│   └── workflows/
│       └── build.yml
└── README.md
```

---

### Task 1: Project Scaffolding — Gradle & Wrapper

**Files:**
- Create: `build.gradle.kts`
- Create: `settings.gradle.kts`
- Create: `gradle.properties`
- Create: `gradle/libs.versions.toml`
- Create: `gradle/wrapper/gradle-wrapper.properties`
- Create: `.gitignore`

- [ ] **Step 1: Create .gitignore**

```gitignore
*.iml
.gradle
/local.properties
/.idea
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
local.properties
/app/build
```

- [ ] **Step 2: Create gradle wrapper properties**

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.5-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

- [ ] **Step 3: Create version catalog `gradle/libs.versions.toml`**

```toml
[versions]
agp = "8.2.2"
kotlin = "1.9.22"
core-ktx = "1.12.0"
activity-compose = "1.8.2"
compose-bom = "2024.02.00"
navigation-compose = "2.7.7"
datastore = "1.0.0"
geckoview = "125.0.20240419211901"
media3 = "1.3.0"
lifecycle = "2.7.0"
junit = "4.13.2"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "core-ktx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activity-compose" }
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-material-icons = { group = "androidx.compose.material", name = "material-icons-extended" }
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation-compose" }
datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
geckoview = { group = "org.mozilla.geckoview", name = "geckoview-default", version.ref = "geckoview" }
media3-exoplayer = { group = "androidx.media3", name = "media3-exoplayer", version.ref = "media3" }
media3-exoplayer-hls = { group = "androidx.media3", name = "media3-exoplayer-hls", version.ref = "media3" }
media3-exoplayer-dash = { group = "androidx.media3", name = "media3-exoplayer-dash", version.ref = "media3" }
media3-ui = { group = "androidx.media3", name = "media3-ui", version.ref = "media3" }
junit = { group = "junit", name = "junit", version.ref = "junit" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
```

- [ ] **Step 4: Create root `build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}
```

- [ ] **Step 5: Create `settings.gradle.kts`**

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolution {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://maven.mozilla.org/maven2/") }
    }
}

rootProject.name = "MiniBrowser"
include(":app")
```

- [ ] **Step 6: Create `gradle.properties`**

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

- [ ] **Step 7: Commit**

```bash
git add .gitignore build.gradle.kts settings.gradle.kts gradle.properties gradle/
git commit -m "chore: scaffold Gradle project with version catalog"
```

---

### Task 2: App Module Build Config

**Files:**
- Create: `app/build.gradle.kts`
- Create: `app/proguard-rules.pro`

- [ ] **Step 1: Create `app/proguard-rules.pro`**

```proguard
# GeckoView
-keep class org.mozilla.geckoview.** { *; }
-dontwarn org.mozilla.geckoview.**
```

- [ ] **Step 2: Create `app/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.minibrowser.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.minibrowser.app"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.navigation.compose)
    implementation(libs.datastore.preferences)

    implementation(libs.geckoview)

    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.hls)
    implementation(libs.media3.exoplayer.dash)
    implementation(libs.media3.ui)

    testImplementation(libs.junit)
}
```

- [ ] **Step 3: Commit**

```bash
git add app/build.gradle.kts app/proguard-rules.pro
git commit -m "chore: add app module build config with all dependencies"
```

---

### Task 3: Android Manifest & Resources

**Files:**
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/values/themes.xml`
- Create: `app/src/main/res/drawable/ic_launcher_foreground.xml`
- Create: `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- Create: `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`
- Create: `app/src/main/res/xml/backup_rules.xml`

- [ ] **Step 1: Create AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />

    <application
        android:name=".MiniBrowserApp"
        android:allowBackup="true"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.MiniBrowser"
        android:usesCleartextTraffic="true">

        <activity
            android:name=".MainActivity"
            android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"
            android:exported="true"
            android:theme="@style/Theme.MiniBrowser">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data android:scheme="http" />
                <data android:scheme="https" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 2: Create `res/values/strings.xml`**

```xml
<resources>
    <string name="app_name">MiniBrowser</string>
    <string name="search_hint">搜索或输入网址</string>
    <string name="go">前往</string>
    <string name="back">后退</string>
    <string name="forward">前进</string>
    <string name="refresh">刷新</string>
    <string name="share">分享</string>
    <string name="search_engine">搜索引擎</string>
    <string name="menu">菜单</string>
</resources>
```

- [ ] **Step 3: Create `res/values/themes.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.MiniBrowser" parent="android:Theme.Material.NoActionBar">
        <item name="android:statusBarColor">#1A1A2E</item>
        <item name="android:navigationBarColor">#1A1A2E</item>
        <item name="android:windowLightStatusBar">false</item>
    </style>
</resources>
```

- [ ] **Step 4: Create launcher icon resources**

`res/drawable/ic_launcher_foreground.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#533483"
        android:pathData="M54,28 C39.64,28 28,39.64 28,54 C28,68.36 39.64,80 54,80 C68.36,80 80,68.36 80,54 C80,39.64 68.36,28 54,28Z" />
    <path
        android:fillColor="#E94560"
        android:pathData="M54,38 L54,54 L66,54 A12,12 0,0,1 54,66 A12,12 0,0,1 42,54 A12,12 0,0,1 54,42Z"
        android:strokeWidth="2"
        android:strokeColor="#EAEAEA" />
    <path
        android:fillColor="#64FFDA"
        android:pathData="M54,44 L54,54 L62,54"
        android:strokeWidth="3"
        android:strokeColor="#64FFDA"
        android:strokeLineCap="round" />
</vector>
```

`res/mipmap-anydpi-v26/ic_launcher.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
```

`res/mipmap-anydpi-v26/ic_launcher_round.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
```

Add to `res/values/strings.xml` (or create `res/values/colors.xml`):

Create `res/values/colors.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="ic_launcher_background">#1A1A2E</color>
</resources>
```

- [ ] **Step 5: Create `res/xml/backup_rules.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<full-backup-content>
    <include domain="sharedpref" path="." />
</full-backup-content>
```

- [ ] **Step 6: Commit**

```bash
git add app/src/main/AndroidManifest.xml app/src/main/res/
git commit -m "chore: add manifest, strings, theme, and launcher icon resources"
```

---

### Task 4: Theme — Color, Typography, Theme Composables

**Files:**
- Create: `app/src/main/java/com/minibrowser/app/ui/theme/Color.kt`
- Create: `app/src/main/java/com/minibrowser/app/ui/theme/Type.kt`
- Create: `app/src/main/java/com/minibrowser/app/ui/theme/Theme.kt`

- [ ] **Step 1: Create `Color.kt`**

```kotlin
package com.minibrowser.app.ui.theme

import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF1A1A2E)
val DarkSurface = Color(0xFF16213E)
val DarkToolbar = Color(0xFF0F3460)
val AccentPurple = Color(0xFF533483)
val AccentRed = Color(0xFFE94560)
val TextPrimary = Color(0xFFEAEAEA)
val TextSecondary = Color(0xFF8892B0)
val LinkColor = Color(0xFF64FFDA)
val ProgressStart = AccentPurple
val ProgressEnd = AccentRed
```

- [ ] **Step 2: Create `Type.kt`**

```kotlin
package com.minibrowser.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val MiniBrowserTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        color = TextSecondary
    ),
    labelSmall = TextStyle(
        fontSize = 12.sp,
        color = TextSecondary
    )
)
```

- [ ] **Step 3: Create `Theme.kt`**

```kotlin
package com.minibrowser.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AccentPurple,
    secondary = AccentRed,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = DarkToolbar,
    onSurfaceVariant = TextSecondary
)

@Composable
fun MiniBrowserTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = MiniBrowserTypography,
        content = content
    )
}
```

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/minibrowser/app/ui/theme/
git commit -m "feat: add dark tech theme with color, typography, and Material3 scheme"
```

---

### Task 5: SearchEngineConfig + UrlUtil (TDD)

**Files:**
- Create: `app/src/main/java/com/minibrowser/app/engine/SearchEngineConfig.kt`
- Create: `app/src/main/java/com/minibrowser/app/engine/UrlUtil.kt`
- Create: `app/src/test/java/com/minibrowser/app/engine/SearchEngineConfigTest.kt`
- Create: `app/src/test/java/com/minibrowser/app/engine/UrlUtilTest.kt`

- [ ] **Step 1: Write failing tests for SearchEngineConfig**

```kotlin
package com.minibrowser.app.engine

import org.junit.Assert.*
import org.junit.Test

class SearchEngineConfigTest {

    @Test
    fun `builtInEngines contains 8 engines`() {
        assertEquals(8, SearchEngineConfig.builtInEngines.size)
    }

    @Test
    fun `default engine is google`() {
        assertEquals("google", SearchEngineConfig.defaultEngineId)
    }

    @Test
    fun `google search url contains query placeholder`() {
        val google = SearchEngineConfig.builtInEngines.first { it.id == "google" }
        assertTrue(google.searchUrl.contains("{query}"))
    }

    @Test
    fun `buildSearchUrl replaces query placeholder`() {
        val google = SearchEngineConfig.builtInEngines.first { it.id == "google" }
        val url = SearchEngineConfig.buildSearchUrl(google, "hello world")
        assertEquals("https://www.google.com/search?q=hello+world", url)
    }

    @Test
    fun `buildSearchUrl encodes special characters`() {
        val google = SearchEngineConfig.builtInEngines.first { it.id == "google" }
        val url = SearchEngineConfig.buildSearchUrl(google, "a&b=c")
        assertTrue(url.contains("a%26b%3Dc") || url.contains("a&b=c").not())
    }

    @Test
    fun `findById returns correct engine`() {
        val engine = SearchEngineConfig.findById("baidu")
        assertNotNull(engine)
        assertEquals("百度", engine!!.name)
    }

    @Test
    fun `findById returns null for unknown id`() {
        assertNull(SearchEngineConfig.findById("nonexistent"))
    }
}
```

- [ ] **Step 2: Write failing tests for UrlUtil**

```kotlin
package com.minibrowser.app.engine

import org.junit.Assert.*
import org.junit.Test

class UrlUtilTest {

    @Test
    fun `url with https protocol is detected as URL`() {
        assertTrue(UrlUtil.isUrl("https://example.com"))
    }

    @Test
    fun `url with http protocol is detected as URL`() {
        assertTrue(UrlUtil.isUrl("http://example.com"))
    }

    @Test
    fun `domain with dot and no spaces is detected as URL`() {
        assertTrue(UrlUtil.isUrl("example.com"))
    }

    @Test
    fun `domain with path is detected as URL`() {
        assertTrue(UrlUtil.isUrl("example.com/path"))
    }

    @Test
    fun `plain text without dot is not URL`() {
        assertFalse(UrlUtil.isUrl("hello world"))
    }

    @Test
    fun `text with dot and spaces is not URL`() {
        assertFalse(UrlUtil.isUrl("hello world.foo bar"))
    }

    @Test
    fun `single word is not URL`() {
        assertFalse(UrlUtil.isUrl("hello"))
    }

    @Test
    fun `smartUrl adds https to bare domain`() {
        assertEquals("https://example.com", UrlUtil.smartUrl("example.com"))
    }

    @Test
    fun `smartUrl preserves existing protocol`() {
        assertEquals("https://example.com", UrlUtil.smartUrl("https://example.com"))
    }

    @Test
    fun `smartUrl trims whitespace`() {
        assertEquals("https://example.com", UrlUtil.smartUrl("  example.com  "))
    }

    @Test
    fun `resolveInput returns url for domain input`() {
        val google = SearchEngineConfig.builtInEngines.first { it.id == "google" }
        val result = UrlUtil.resolveInput("example.com", google)
        assertEquals("https://example.com", result)
    }

    @Test
    fun `resolveInput returns search url for query input`() {
        val google = SearchEngineConfig.builtInEngines.first { it.id == "google" }
        val result = UrlUtil.resolveInput("hello world", google)
        assertEquals("https://www.google.com/search?q=hello+world", result)
    }
}
```

- [ ] **Step 3: Run tests to verify they fail**

Run: `cd /Users/mi/Desktop/mini_browser && ./gradlew :app:testDebugUnitTest --tests "com.minibrowser.app.engine.*" 2>&1 | tail -5`
Expected: Compilation failure — classes don't exist yet.

- [ ] **Step 4: Implement SearchEngineConfig**

```kotlin
package com.minibrowser.app.engine

import java.net.URLEncoder

data class SearchEngine(
    val id: String,
    val name: String,
    val searchUrl: String,
    val homeUrl: String
)

object SearchEngineConfig {

    const val defaultEngineId = "google"

    val builtInEngines = listOf(
        SearchEngine("google", "Google", "https://www.google.com/search?q={query}", "https://www.google.com"),
        SearchEngine("bing", "Bing", "https://www.bing.com/search?q={query}", "https://www.bing.com"),
        SearchEngine("duckduckgo", "DuckDuckGo", "https://duckduckgo.com/?q={query}", "https://duckduckgo.com"),
        SearchEngine("baidu", "百度", "https://www.baidu.com/s?wd={query}", "https://www.baidu.com"),
        SearchEngine("yahoo", "Yahoo", "https://search.yahoo.com/search?p={query}", "https://search.yahoo.com"),
        SearchEngine("yandex", "Yandex", "https://yandex.com/search/?text={query}", "https://yandex.com"),
        SearchEngine("sogou", "搜狗", "https://www.sogou.com/web?query={query}", "https://www.sogou.com"),
        SearchEngine("so360", "360搜索", "https://www.so.com/s?q={query}", "https://www.so.com")
    )

    fun findById(id: String): SearchEngine? = builtInEngines.find { it.id == id }

    fun buildSearchUrl(engine: SearchEngine, query: String): String {
        val encoded = URLEncoder.encode(query, "UTF-8").replace("%20", "+")
        return engine.searchUrl.replace("{query}", encoded)
    }
}
```

- [ ] **Step 5: Implement UrlUtil**

```kotlin
package com.minibrowser.app.engine

object UrlUtil {

    private val urlPattern = Regex(
        "^(https?://)?[\\w\\-]+(\\.[\\w\\-]+)+(/\\S*)?$",
        RegexOption.IGNORE_CASE
    )

    fun isUrl(input: String): Boolean {
        val trimmed = input.trim()
        if (trimmed.contains(" ")) return false
        return urlPattern.matches(trimmed)
    }

    fun smartUrl(input: String): String {
        val trimmed = input.trim()
        return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "https://$trimmed"
        }
    }

    fun resolveInput(input: String, engine: SearchEngine): String {
        val trimmed = input.trim()
        return if (isUrl(trimmed)) {
            smartUrl(trimmed)
        } else {
            SearchEngineConfig.buildSearchUrl(engine, trimmed)
        }
    }
}
```

- [ ] **Step 6: Run tests to verify they pass**

Run: `cd /Users/mi/Desktop/mini_browser && ./gradlew :app:testDebugUnitTest --tests "com.minibrowser.app.engine.*" 2>&1 | tail -20`
Expected: All 14 tests pass.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/minibrowser/app/engine/SearchEngineConfig.kt \
       app/src/main/java/com/minibrowser/app/engine/UrlUtil.kt \
       app/src/test/java/com/minibrowser/app/engine/
git commit -m "feat: add SearchEngineConfig and UrlUtil with unit tests"
```

---

### Task 6: DataStore Preferences Repository

**Files:**
- Create: `app/src/main/java/com/minibrowser/app/data/PreferencesRepository.kt`

- [ ] **Step 1: Implement PreferencesRepository**

```kotlin
package com.minibrowser.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.minibrowser.app.engine.SearchEngineConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesRepository(private val context: Context) {

    private val engineKey = stringPreferencesKey("search_engine_id")

    val selectedEngineId: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[engineKey] ?: SearchEngineConfig.defaultEngineId
    }

    suspend fun setSearchEngine(engineId: String) {
        context.dataStore.edit { prefs ->
            prefs[engineKey] = engineId
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/minibrowser/app/data/PreferencesRepository.kt
git commit -m "feat: add DataStore preferences repository for engine selection"
```

---

### Task 7: GeckoEngineManager

**Files:**
- Create: `app/src/main/java/com/minibrowser/app/engine/GeckoEngineManager.kt`
- Create: `app/src/main/java/com/minibrowser/app/MiniBrowserApp.kt`

- [ ] **Step 1: Implement GeckoEngineManager**

```kotlin
package com.minibrowser.app.engine

import android.content.Context
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSession.ContentDelegate
import org.mozilla.geckoview.GeckoSession.NavigationDelegate
import org.mozilla.geckoview.GeckoSession.ProgressDelegate

class GeckoEngineManager(context: Context) {

    val runtime: GeckoRuntime = GeckoRuntime.create(
        context,
        GeckoRuntimeSettings.Builder()
            .javaScriptEnabled(true)
            .consoleOutput(false)
            .build()
    )

    private var session: GeckoSession? = null

    var onTitleChanged: ((String) -> Unit)? = null
    var onUrlChanged: ((String) -> Unit)? = null
    var onProgressChanged: ((Int) -> Unit)? = null
    var onCanGoBackChanged: ((Boolean) -> Unit)? = null
    var onCanGoForwardChanged: ((Boolean) -> Unit)? = null
    var onFullScreenRequest: ((Boolean) -> Unit)? = null

    fun createSession(): GeckoSession {
        session?.close()
        val newSession = GeckoSession()

        newSession.contentDelegate = object : ContentDelegate {
            override fun onTitleChange(session: GeckoSession, title: String?) {
                title?.let { onTitleChanged?.invoke(it) }
            }

            override fun onFullScreen(session: GeckoSession, fullScreen: Boolean) {
                onFullScreenRequest?.invoke(fullScreen)
            }
        }

        newSession.navigationDelegate = object : NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>,
                hasUserGesture: Boolean
            ) {
                url?.let { onUrlChanged?.invoke(it) }
            }

            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
                onCanGoBackChanged?.invoke(canGoBack)
            }

            override fun onCanGoForward(session: GeckoSession, canGoForward: Boolean) {
                onCanGoForwardChanged?.invoke(canGoForward)
            }
        }

        newSession.progressDelegate = object : ProgressDelegate {
            override fun onProgressChange(session: GeckoSession, progress: Int) {
                onProgressChanged?.invoke(progress)
            }
        }

        newSession.open(runtime)
        this.session = newSession
        return newSession
    }

    fun loadUrl(url: String) {
        session?.loadUri(url)
    }

    fun goBack() {
        session?.goBack()
    }

    fun goForward() {
        session?.goForward()
    }

    fun reload() {
        session?.reload()
    }

    fun getVideoUrl(callback: (String?) -> Unit) {
        session?.evaluateJavascript(
            "(function() { var v = document.querySelector('video'); return v ? v.src || v.currentSrc : null; })()"
        )?.then { result ->
            val url = result?.asJSValue()?.asString()
            callback(url)
            null
        }
    }

    fun close() {
        session?.close()
        session = null
    }
}
```

- [ ] **Step 2: Implement MiniBrowserApp**

```kotlin
package com.minibrowser.app

import android.app.Application
import com.minibrowser.app.data.PreferencesRepository
import com.minibrowser.app.engine.GeckoEngineManager

class MiniBrowserApp : Application() {

    lateinit var geckoEngineManager: GeckoEngineManager
        private set
    lateinit var preferencesRepository: PreferencesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        geckoEngineManager = GeckoEngineManager(this)
        preferencesRepository = PreferencesRepository(this)
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/minibrowser/app/engine/GeckoEngineManager.kt \
       app/src/main/java/com/minibrowser/app/MiniBrowserApp.kt
git commit -m "feat: add GeckoEngineManager with session lifecycle and MiniBrowserApp"
```

---

### Task 8: VideoPlayerManager

**Files:**
- Create: `app/src/main/java/com/minibrowser/app/player/VideoPlayerManager.kt`

- [ ] **Step 1: Implement VideoPlayerManager**

```kotlin
package com.minibrowser.app.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource

@OptIn(UnstableApi::class)
class VideoPlayerManager(context: Context) {

    val player: ExoPlayer = ExoPlayer.Builder(context).build()

    private val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        .setUserAgent("MiniBrowser/1.0")

    fun play(url: String) {
        val mediaSource = buildMediaSource(url)
        player.setMediaSource(mediaSource)
        player.prepare()
        player.playWhenReady = true
    }

    private fun buildMediaSource(url: String): MediaSource {
        val mediaItem = MediaItem.fromUri(url)
        return when {
            url.contains(".m3u8", ignoreCase = true) ->
                HlsMediaSource.Factory(httpDataSourceFactory).createMediaSource(mediaItem)
            url.contains(".mpd", ignoreCase = true) ->
                DashMediaSource.Factory(httpDataSourceFactory).createMediaSource(mediaItem)
            else ->
                ProgressiveMediaSource.Factory(httpDataSourceFactory).createMediaSource(mediaItem)
        }
    }

    fun stop() {
        player.stop()
        player.clearMediaItems()
    }

    fun release() {
        player.release()
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/minibrowser/app/player/VideoPlayerManager.kt
git commit -m "feat: add VideoPlayerManager with HLS/DASH/progressive support"
```

---

### Task 9: UI Components — SearchBar

**Files:**
- Create: `app/src/main/java/com/minibrowser/app/ui/components/SearchBar.kt`

- [ ] **Step 1: Implement SearchBar**

```kotlin
package com.minibrowser.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minibrowser.app.ui.theme.DarkToolbar
import com.minibrowser.app.ui.theme.TextPrimary
import com.minibrowser.app.ui.theme.TextSecondary

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    placeholder: String = "搜索或输入网址",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(DarkToolbar, RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = placeholder,
                    color = TextSecondary,
                    fontSize = 15.sp
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(color = TextPrimary, fontSize = 15.sp),
                cursorBrush = SolidColor(TextPrimary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { onSubmit() }),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (query.isNotEmpty()) {
            IconButton(onClick = onSubmit) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Go",
                    tint = TextPrimary
                )
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/minibrowser/app/ui/components/SearchBar.kt
git commit -m "feat: add SearchBar composable with keyboard submit"
```

---

### Task 10: UI Components — SearchEngineSelector

**Files:**
- Create: `app/src/main/java/com/minibrowser/app/ui/components/SearchEngineSelector.kt`

- [ ] **Step 1: Implement SearchEngineSelector**

```kotlin
package com.minibrowser.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minibrowser.app.engine.SearchEngine
import com.minibrowser.app.engine.SearchEngineConfig
import com.minibrowser.app.ui.theme.AccentPurple
import com.minibrowser.app.ui.theme.DarkSurface
import com.minibrowser.app.ui.theme.TextPrimary
import com.minibrowser.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchEngineSelector(
    selectedEngineId: String,
    onEngineSelected: (SearchEngine) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface
    ) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Text(
                text = "选择搜索引擎",
                color = TextPrimary,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
            SearchEngineConfig.builtInEngines.forEach { engine ->
                val isSelected = engine.id == selectedEngineId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEngineSelected(engine) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = if (isSelected) AccentPurple else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = engine.name,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = AccentPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/minibrowser/app/ui/components/SearchEngineSelector.kt
git commit -m "feat: add SearchEngineSelector bottom sheet component"
```

---

### Task 11: UI Components — BrowserView (GeckoView Wrapper)

**Files:**
- Create: `app/src/main/java/com/minibrowser/app/ui/components/BrowserView.kt`

- [ ] **Step 1: Implement BrowserView**

```kotlin
package com.minibrowser.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.minibrowser.app.MiniBrowserApp
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

@Composable
fun BrowserView(
    session: GeckoSession,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as MiniBrowserApp

    AndroidView(
        factory = { ctx ->
            GeckoView(ctx).apply {
                setSession(session)
            }
        },
        modifier = modifier
    )
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/minibrowser/app/ui/components/BrowserView.kt
git commit -m "feat: add BrowserView GeckoView-Compose interop wrapper"
```

---

### Task 12: UI Components — VideoPlayer (ExoPlayer Wrapper)

**Files:**
- Create: `app/src/main/java/com/minibrowser/app/ui/components/VideoPlayer.kt`

- [ ] **Step 1: Implement VideoPlayer**

```kotlin
package com.minibrowser.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.minibrowser.app.player.VideoPlayerManager

@Composable
fun VideoPlayer(
    videoUrl: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val playerManager = remember { VideoPlayerManager(context) }

    DisposableEffect(videoUrl) {
        playerManager.play(videoUrl)
        onDispose {
            playerManager.stop()
            playerManager.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = playerManager.player
                useController = true
            }
        },
        modifier = modifier
    )
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/minibrowser/app/ui/components/VideoPlayer.kt
git commit -m "feat: add VideoPlayer ExoPlayer-Compose wrapper"
```

---

### Task 13: HomeScreen

**Files:**
- Create: `app/src/main/java/com/minibrowser/app/ui/screens/HomeScreen.kt`

- [ ] **Step 1: Implement HomeScreen**

```kotlin
package com.minibrowser.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minibrowser.app.engine.SearchEngine
import com.minibrowser.app.engine.SearchEngineConfig
import com.minibrowser.app.ui.components.SearchBar
import com.minibrowser.app.ui.components.SearchEngineSelector
import com.minibrowser.app.ui.theme.AccentPurple
import com.minibrowser.app.ui.theme.DarkBackground
import com.minibrowser.app.ui.theme.DarkSurface
import com.minibrowser.app.ui.theme.TextPrimary
import com.minibrowser.app.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    selectedEngineId: String,
    onNavigate: (String) -> Unit,
    onEngineSelected: (SearchEngine) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var showEngineSelector by remember { mutableStateOf(false) }
    val currentEngine = SearchEngineConfig.findById(selectedEngineId)
        ?: SearchEngineConfig.builtInEngines.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(120.dp))

        Text(
            text = "MiniBrowser",
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = currentEngine.name,
            color = AccentPurple,
            fontSize = 14.sp,
            modifier = Modifier.clickable { showEngineSelector = true }
        )

        Spacer(Modifier.height(32.dp))

        SearchBar(
            query = query,
            onQueryChange = { query = it },
            onSubmit = {
                if (query.isNotBlank()) {
                    onNavigate(query.trim())
                }
            }
        )

        Spacer(Modifier.height(40.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(SearchEngineConfig.builtInEngines) { engine ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        onNavigate(engine.homeUrl)
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(DarkSurface, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = engine.name,
                            tint = AccentPurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = engine.name,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }

    if (showEngineSelector) {
        SearchEngineSelector(
            selectedEngineId = selectedEngineId,
            onEngineSelected = { engine ->
                onEngineSelected(engine)
                showEngineSelector = false
            },
            onDismiss = { showEngineSelector = false }
        )
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/minibrowser/app/ui/screens/HomeScreen.kt
git commit -m "feat: add HomeScreen with search bar and engine grid"
```

---

### Task 14: BrowserScreen

**Files:**
- Create: `app/src/main/java/com/minibrowser/app/ui/screens/BrowserScreen.kt`

- [ ] **Step 1: Implement BrowserScreen**

```kotlin
package com.minibrowser.app.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minibrowser.app.MiniBrowserApp
import com.minibrowser.app.engine.SearchEngine
import com.minibrowser.app.engine.SearchEngineConfig
import com.minibrowser.app.engine.UrlUtil
import com.minibrowser.app.ui.components.BrowserView
import com.minibrowser.app.ui.components.SearchEngineSelector
import com.minibrowser.app.ui.components.VideoPlayer
import com.minibrowser.app.ui.theme.AccentPurple
import com.minibrowser.app.ui.theme.AccentRed
import com.minibrowser.app.ui.theme.DarkBackground
import com.minibrowser.app.ui.theme.DarkToolbar
import com.minibrowser.app.ui.theme.ProgressEnd
import com.minibrowser.app.ui.theme.ProgressStart
import com.minibrowser.app.ui.theme.TextPrimary
import com.minibrowser.app.ui.theme.TextSecondary
import org.mozilla.geckoview.GeckoSession

@Composable
fun BrowserScreen(
    initialInput: String,
    selectedEngineId: String,
    onEngineSelected: (SearchEngine) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as MiniBrowserApp
    val engineManager = app.geckoEngineManager
    val currentEngine = SearchEngineConfig.findById(selectedEngineId)
        ?: SearchEngineConfig.builtInEngines.first()

    var currentUrl by remember { mutableStateOf("") }
    var urlBarText by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var progress by remember { mutableIntStateOf(0) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showEngineSelector by remember { mutableStateOf(false) }
    var isFullScreen by remember { mutableStateOf(false) }
    var videoUrl by remember { mutableStateOf<String?>(null) }

    val session = remember {
        engineManager.apply {
            onUrlChanged = { url ->
                currentUrl = url
                if (!isEditing) urlBarText = url
            }
            onProgressChanged = { p -> progress = p }
            onCanGoBackChanged = { canGoBack = it }
            onCanGoForwardChanged = { canGoForward = it }
            onFullScreenRequest = { fullScreen ->
                isFullScreen = fullScreen
                if (fullScreen) {
                    engineManager.getVideoUrl { url ->
                        videoUrl = url
                    }
                } else {
                    videoUrl = null
                }
            }
        }
        engineManager.createSession()
    }

    LaunchedEffect(initialInput) {
        val url = UrlUtil.resolveInput(initialInput, currentEngine)
        engineManager.loadUrl(url)
    }

    DisposableEffect(Unit) {
        onDispose { }
    }

    if (isFullScreen && videoUrl != null) {
        VideoPlayer(
            videoUrl = videoUrl!!,
            onDismiss = {
                isFullScreen = false
                videoUrl = null
            },
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkToolbar)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (canGoBack) engineManager.goBack() else onBack()
                }
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (canGoBack) TextPrimary else TextSecondary
                )
            }

            IconButton(
                onClick = { engineManager.goForward() },
                enabled = canGoForward
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Forward",
                    tint = if (canGoForward) TextPrimary else TextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(DarkBackground.copy(alpha = 0.5f), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                BasicTextField(
                    value = urlBarText,
                    onValueChange = {
                        urlBarText = it
                        isEditing = true
                    },
                    singleLine = true,
                    textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                    cursorBrush = SolidColor(TextPrimary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            isEditing = false
                            val url = UrlUtil.resolveInput(urlBarText, currentEngine)
                            engineManager.loadUrl(url)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = TextPrimary
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("刷新") },
                        leadingIcon = { Icon(Icons.Default.Refresh, null) },
                        onClick = {
                            engineManager.reload()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("搜索引擎") },
                        onClick = {
                            showEngineSelector = true
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("分享") },
                        leadingIcon = { Icon(Icons.Default.Share, null) },
                        onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, currentUrl)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, null))
                            showMenu = false
                        }
                    )
                }
            }
        }

        // Progress bar
        AnimatedVisibility(
            visible = progress in 1..99,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = AccentRed,
                trackColor = DarkToolbar
            )
        }

        // Web content
        BrowserView(
            session = session,
            modifier = Modifier
                .fillMaxSize()
        )
    }

    if (showEngineSelector) {
        SearchEngineSelector(
            selectedEngineId = selectedEngineId,
            onEngineSelected = { engine ->
                onEngineSelected(engine)
                showEngineSelector = false
            },
            onDismiss = { showEngineSelector = false }
        )
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/minibrowser/app/ui/screens/BrowserScreen.kt
git commit -m "feat: add BrowserScreen with toolbar, progress bar, and video fullscreen"
```

---

### Task 15: Navigation Graph

**Files:**
- Create: `app/src/main/java/com/minibrowser/app/ui/navigation/NavGraph.kt`

- [ ] **Step 1: Implement NavGraph**

```kotlin
package com.minibrowser.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.minibrowser.app.MiniBrowserApp
import com.minibrowser.app.ui.screens.BrowserScreen
import com.minibrowser.app.ui.screens.HomeScreen
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.net.URLEncoder

object Routes {
    const val HOME = "home"
    const val BROWSER = "browser/{input}"

    fun browser(input: String): String {
        val encoded = URLEncoder.encode(input, "UTF-8")
        return "browser/$encoded"
    }
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as MiniBrowserApp
    val scope = rememberCoroutineScope()

    val selectedEngineId by app.preferencesRepository.selectedEngineId
        .collectAsState(initial = "google")

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                selectedEngineId = selectedEngineId,
                onNavigate = { input ->
                    navController.navigate(Routes.browser(input))
                },
                onEngineSelected = { engine ->
                    scope.launch {
                        app.preferencesRepository.setSearchEngine(engine.id)
                    }
                }
            )
        }
        composable(
            route = Routes.BROWSER,
            arguments = listOf(navArgument("input") { type = NavType.StringType })
        ) { backStackEntry ->
            val input = URLDecoder.decode(
                backStackEntry.arguments?.getString("input") ?: "",
                "UTF-8"
            )
            BrowserScreen(
                initialInput = input,
                selectedEngineId = selectedEngineId,
                onEngineSelected = { engine ->
                    scope.launch {
                        app.preferencesRepository.setSearchEngine(engine.id)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/minibrowser/app/ui/navigation/NavGraph.kt
git commit -m "feat: add navigation graph with Home and Browser routes"
```

---

### Task 16: MainActivity

**Files:**
- Create: `app/src/main/java/com/minibrowser/app/MainActivity.kt`

- [ ] **Step 1: Implement MainActivity**

```kotlin
package com.minibrowser.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.minibrowser.app.ui.navigation.NavGraph
import com.minibrowser.app.ui.theme.DarkBackground
import com.minibrowser.app.ui.theme.MiniBrowserTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiniBrowserTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    NavGraph()
                }
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/minibrowser/app/MainActivity.kt
git commit -m "feat: add MainActivity with Compose entry point"
```

---

### Task 17: GitHub Actions CI

**Files:**
- Create: `.github/workflows/build.yml`

- [ ] **Step 1: Implement build workflow**

```yaml
name: Build APK

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Build Release APK
        run: ./gradlew assembleRelease

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: app-release
          path: app/build/outputs/apk/release/app-release-unsigned.apk
```

- [ ] **Step 2: Commit**

```bash
git add .github/workflows/build.yml
git commit -m "ci: add GitHub Actions workflow for APK build"
```

---

### Task 18: README

**Files:**
- Create: `README.md`

- [ ] **Step 1: Write README**

```markdown
# MiniBrowser

Android 极简浏览器 — GeckoView + Jetpack Compose + Material 3

## 功能

- GeckoView (Firefox 内核) 网页渲染
- 智能 URL / 搜索分流
- 8 个内置搜索引擎可切换
- ExoPlayer 视频全屏播放 (HLS/DASH/MP4)
- 深色科技风 UI
- DataStore 偏好持久化

## 技术栈

| 组件 | 版本 |
|------|------|
| Kotlin | 1.9.22 |
| Compose BOM | 2024.02.00 |
| GeckoView | 125.0 |
| Media3 ExoPlayer | 1.3.0 |
| Min SDK | 29 (Android 10) |
| Target SDK | 34 (Android 14) |

## 构建

```bash
./gradlew assembleDebug
```

## CI

Push 到 `main` 或提交 PR 时自动构建 Release APK。
```

- [ ] **Step 2: Commit**

```bash
git add README.md
git commit -m "docs: add project README"
```

---

### Task 19: Gradle Wrapper JAR + gradlew Scripts

**Files:**
- Create: `gradle/wrapper/gradle-wrapper.jar` (via gradle wrapper task or copy)
- Create: `gradlew`
- Create: `gradlew.bat`

- [ ] **Step 1: Generate Gradle wrapper**

Run this from the project root (requires a Gradle installation or use an existing project's wrapper):

```bash
cd /Users/mi/Desktop/mini_browser
gradle wrapper --gradle-version 8.5
```

If `gradle` CLI is not available, copy `gradlew`, `gradlew.bat`, and `gradle/wrapper/gradle-wrapper.jar` from any recent Android project or download from the Gradle distributions.

- [ ] **Step 2: Verify wrapper works**

```bash
./gradlew --version
```

Expected: Gradle 8.5 output.

- [ ] **Step 3: Commit**

```bash
git add gradlew gradlew.bat gradle/
git commit -m "chore: add Gradle 8.5 wrapper"
```

---

### Task 20: Integration Verification

- [ ] **Step 1: Run unit tests**

```bash
cd /Users/mi/Desktop/mini_browser
./gradlew :app:testDebugUnitTest 2>&1 | tail -20
```

Expected: All tests pass (UrlUtilTest, SearchEngineConfigTest).

- [ ] **Step 2: Attempt debug build**

```bash
./gradlew assembleDebug 2>&1 | tail -10
```

Expected: BUILD SUCCESSFUL. APK at `app/build/outputs/apk/debug/app-debug.apk`.

- [ ] **Step 3: Verify APK exists**

```bash
ls -lh app/build/outputs/apk/debug/app-debug.apk
```

Expected: File exists, approximately 25-35 MB.
