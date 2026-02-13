import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.roborazzi)
    id("jacoco")
}

android {
    namespace = "com.flit.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "app.flit.mobile"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.flit.app.HiltTestRunner"
    }

    val debugApiBaseUrl = (project.findProperty("FLIT_API_BASE_URL") as String?)
        ?: "https://gifted-michiko-auric.ngrok-free.dev/api/v1"
    val benchmarkApiBaseUrl = (project.findProperty("FLIT_BENCHMARK_API_BASE_URL") as String?)
        ?: debugApiBaseUrl
    val googleAndroidClientId = (project.findProperty("GOOGLE_ANDROID_CLIENT_ID") as String?)
        ?: ""
    val googleWebClientId = (project.findProperty("GOOGLE_WEB_CLIENT_ID") as String?)
        ?: ""

    // Keystore 설정 로드 (릴리스 서명용)
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties = Properties()
    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    }

    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties.getProperty("keyAlias") ?: ""
            keyPassword = keystoreProperties.getProperty("keyPassword") ?: ""
            // Resolve path from project root so `keystore.properties` can use `flit-upload.jks`.
            storeFile = keystoreProperties.getProperty("storeFile")?.let { path -> rootProject.file(path) }
            storePassword = keystoreProperties.getProperty("storePassword") ?: ""
        }
    }

    buildTypes {
        debug {
            // 기본값은 Android 에뮬레이터(localhost -> 10.0.2.2)
            // 필요 시 -PFLIT_API_BASE_URL=https://... 로 오버라이드 가능
            buildConfigField("String", "API_BASE_URL", "\"$debugApiBaseUrl\"")
            buildConfigField("String", "GOOGLE_ANDROID_CLIENT_ID", "\"$googleAndroidClientId\"")
            buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")
            buildConfigField("boolean", "ALLOW_DESTRUCTIVE_MIGRATION", "true")
        }
        create("benchmark") {
            initWith(getByName("release"))
            matchingFallbacks += listOf("release")
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = false
            buildConfigField("String", "API_BASE_URL", "\"$benchmarkApiBaseUrl\"")
            buildConfigField("String", "GOOGLE_ANDROID_CLIENT_ID", "\"$googleAndroidClientId\"")
            buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")
            buildConfigField("boolean", "ALLOW_DESTRUCTIVE_MIGRATION", "true")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("String", "API_BASE_URL", "\"https://api.flit.app/api/v1\"")
            buildConfigField("String", "GOOGLE_ANDROID_CLIENT_ID", "\"$googleAndroidClientId\"")
            buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")
            buildConfigField("boolean", "ALLOW_DESTRUCTIVE_MIGRATION", "false")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    // Unit 테스트에서 Android 프레임워크 메서드 호출 시 기본값 반환
    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

jacoco {
    toolVersion = "0.8.12"
}

val coverageGateEnabled = providers.environmentVariable("CI")
    .map { true }
    .orElse(
        providers.gradleProperty("coverageGate").map { it.toBoolean() }
    )
    .getOrElse(false)

val coverageLineMinimum = providers.gradleProperty("coverageLineMinimum")
    .orElse("0.60")
    .map { it.toBigDecimal() }
    .get()

val jacocoExclusions = listOf(
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Test*.*",
    "**/*\$Lambda$*.*",
    "**/*\$inlined$*.*",
    "**/*\$Companion*.*",
    "**/di/**",
    "**/data/worker/**"
)

val debugKotlinClasses = fileTree("${layout.buildDirectory.get().asFile}/tmp/kotlin-classes/debug") {
    include("com/flit/app/domain/usecase/**")
    include("com/flit/app/presentation/**/*ViewModel*.class")
    exclude(jacocoExclusions)
}

val debugJavaClasses = fileTree("${layout.buildDirectory.get().asFile}/intermediates/javac/debug/classes") {
    include("com/flit/app/domain/usecase/**")
    include("com/flit/app/presentation/**/*ViewModel*.class")
    exclude(jacocoExclusions)
}

tasks.withType<Test>().configureEach {
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    classDirectories.setFrom(files(debugKotlinClasses, debugJavaClasses))
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(
        fileTree(layout.buildDirectory.get().asFile) {
            include("jacoco/testDebugUnitTest.exec")
            include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        }
    )
}

tasks.register<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn("testDebugUnitTest")

    classDirectories.setFrom(files(debugKotlinClasses, debugJavaClasses))
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(
        fileTree(layout.buildDirectory.get().asFile) {
            include("jacoco/testDebugUnitTest.exec")
            include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        }
    )

    violationRules {
        rule {
            element = "BUNDLE"
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = coverageLineMinimum
            }
        }
    }
}

tasks.named("check") {
    dependsOn("testDebugUnitTest")
    dependsOn("jacocoTestReport")
    if (coverageGateEnabled) {
        dependsOn("jacocoTestCoverageVerification")
    }
}

baselineProfile {
    dexLayoutOptimization = true
}

dependencies {

    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Gson
    implementation(libs.gson)

    // Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // WorkManager
    implementation(libs.work.runtime.ktx)

    // Lifecycle
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    // Navigation
    implementation(libs.navigation.compose)

    // CameraX
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)

    // Image loading
    implementation(libs.coil.compose)

    // DataStore
    implementation(libs.datastore.preferences)

    // Glance (위젯)
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)

    // Security (EncryptedSharedPreferences)
    implementation(libs.security.crypto)
    implementation(libs.credentials)
    implementation(libs.credentials.play)
    implementation(libs.googleid)

    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.tracing.ktx)
    implementation(libs.compose.runtime.tracing)

    // Google Play Billing
    implementation("com.android.billingclient:billing-ktx:7.0.0")

    // Testing - Unit Tests
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.arch.core.testing)
    testImplementation(libs.work.testing)

    // Roborazzi (스크린샷 테스트)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.rule)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(platform(libs.androidx.compose.bom))

    // Testing - Instrumented Tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.navigation.testing)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.coroutines.test)
    androidTestImplementation(libs.turbine)
    androidTestImplementation(libs.work.testing)
    kspAndroidTest(libs.hilt.compiler)

    // Debug implementations
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    baselineProfile(project(":baselineprofile"))
}
