# Senswear Production ProGuard & R8 Optimization Rules

# KotlinX Serialization
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,allowobfuscation,allowshrinking class * {
    <fields>;
}

# Room / SQLite
-keep class * extends android.database.sqlite.SQLiteOpenHelper { *; }
-keepclassmembers class com.senswear.app.core.data.local.entity.** { *; }

# Google Health Connect
-keep class androidx.health.connect.client.** { *; }
-keep class androidx.health.platform.client.** { *; }

# Compose Runtime
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# Domain Models
-keep class com.senswear.app.core.domain.model.** { *; }
-keep class com.senswear.app.core.wearable.** { *; }

# Strip logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}
