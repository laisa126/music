# Aurora Music — R8 rules

# Kotlin metadata / coroutines
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses, Signature, RuntimeVisibleAnnotations, AnnotationDefault
-dontnote kotlinx.serialization.**
-keepclasseswithmembers class **$$serializer { *** INSTANCE; }
-keepclassmembers @kotlinx.serialization.Serializable class * {
    static **$* *;
    static *** Companion;
    *** Companion(...);
}

# Retrofit / OkHttp (Phase 2 scaffolding)
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-keepattributes Signature, Exceptions
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Media3
-dontwarn androidx.media3.**
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.session.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-dontwarn androidx.room.paging.**

# Hilt / Dagger
-dontwarn com.google.errorprone.annotations.**

# Glance / AppWidget receivers referenced from the manifest only
-keep class com.aurora.music.widget.** { *; }
-keep class com.aurora.music.tile.** { *; }

# Compose
-dontwarn androidx.compose.**
