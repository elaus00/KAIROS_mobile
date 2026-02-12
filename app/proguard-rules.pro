# Flit. Mobile ProGuard 규칙
# 릴리스 빌드 시 코드 난독화 및 최적화 설정

# ============================================================
# 기본 설정
# ============================================================

# 스택 트레이스 디버깅을 위한 라인 번호 정보 유지
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Annotation 유지 (Retrofit, Room, Hilt 등에 필요)
-keepattributes *Annotation*

# Signature 유지 (제네릭 타입 정보)
-keepattributes Signature

# Exception 유지 (에러 추적용)
-keepattributes Exceptions

# InnerClasses 유지
-keepattributes InnerClasses

# EnclosingMethod 유지 (람다/익명 클래스용)
-keepattributes EnclosingMethod

# ============================================================
# Kotlin
# ============================================================

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# Kotlin Metadata
-keep class kotlin.Metadata { *; }

# ============================================================
# Retrofit + OkHttp
# ============================================================

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-dontwarn org.codehaus.mojo.animal_sniffer.*

# ============================================================
# Gson
# ============================================================

# Gson 일반 규칙
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**

# Gson 사용 클래스 (data class, DTO)
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements java.lang.reflect.Type

# DTO (Data Transfer Object) - API 요청/응답 모델 유지
-keep class com.flit.app.data.remote.dto.** { *; }

# Entity - Room DB 엔티티 유지
-keep class com.flit.app.data.local.database.entities.** { *; }

# Domain Models - 도메인 모델 유지
-keep class com.flit.app.domain.model.** { *; }

# ============================================================
# Room Database
# ============================================================

-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# DAO 인터페이스 유지
-keep interface com.flit.app.data.local.database.dao.** { *; }

# Room 컴파일러 생성 구현체 유지
-keep class com.flit.app.data.local.database.FlitDatabase_Impl { *; }

# ============================================================
# Hilt / Dagger
# ============================================================

-dontwarn com.google.errorprone.annotations.*
-dontwarn javax.inject.**
-dontwarn dagger.hilt.internal.**

-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent

# Hilt 모듈 유지
-keep class com.flit.app.di.** { *; }

# Hilt ViewModels
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ============================================================
# Jetpack Compose
# ============================================================

# Compose Runtime
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.runtime.**

# Compose UI
-keep class androidx.compose.ui.** { *; }

# Composable 함수 유지
-keep @androidx.compose.runtime.Composable class * { *; }
-keep @androidx.compose.runtime.Composable interface * { *; }

# ============================================================
# WorkManager
# ============================================================

-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker {
    public <init>(...);
}

# Worker 클래스 유지
-keep class com.flit.app.data.worker.** { *; }

-keepclassmembers class * extends androidx.work.Worker {
    public <init>(android.content.Context,androidx.work.WorkerParameters);
}

# ============================================================
# Google Play Billing
# ============================================================

-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# ============================================================
# Security (EncryptedSharedPreferences)
# ============================================================

-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# ============================================================
# Android Components
# ============================================================

# Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Enum
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ============================================================
# CameraX
# ============================================================

-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# ============================================================
# Coil (Image Loading)
# ============================================================

-keep class coil.** { *; }
-dontwarn coil.**

# ============================================================
# Chrome Custom Tabs
# ============================================================

-keep class androidx.browser.** { *; }
-dontwarn androidx.browser.**

# ============================================================
# 최적화 설정
# ============================================================

# 공격적인 최적화 비활성화 (안정성 우선)
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# R8 전체 프로그램 최적화 허용
-allowaccessmodification

# 이미 개별 라이브러리별로 dontwarn 규칙을 적용했으므로
# 전역 dontwarn은 사용하지 않음 (R8 경고로 실제 문제 감지 가능)