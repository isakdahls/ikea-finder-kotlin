# IKEA Finder ProGuard Rules
# Regler för OSMDroid och Room kompatibilitet i release builds

# Keep source file information for debugging
-keepattributes SourceFile,LineNumberTable

# Room Database - bevara entiteter och DAO:er
-keep class se.isakdahls.ikeafinder.data.database.** { *; }
-keep class se.isakdahls.ikeafinder.data.models.** { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep class * extends androidx.room.RoomDatabase
-keep class androidx.room.** { *; }

# OSMDroid - bevara kartfunktionalitet
-keep class org.osmdroid.** { *; }
-keep class microsoft.mappoint.** { *; }
-dontwarn org.osmdroid.**
-dontwarn microsoft.mappoint.**

# Location Services - bevara GPS funktionalitet
-keep class com.google.android.gms.location.** { *; }
-keep class com.google.android.gms.common.** { *; }
-dontwarn com.google.android.gms.**

# Compose - bevara UI komponenter
-keep class androidx.compose.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn androidx.compose.**

# Navigation - bevara routing
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

# Kotlin Coroutines - bevara async funktionalitet  
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Asset Manager och Utilities - bevara våra hjälpklasser
-keep class se.isakdahls.ikeafinder.utils.** { *; }

# ViewModel och Lifecycle - bevara state management
-keep class androidx.lifecycle.** { *; }
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Reflection för Room och andra bibliotek
-keepclassmembers class * {
    @androidx.room.** <methods>;
    @androidx.room.** <fields>;
}

# Serialization och Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# Enum classes - bevara för säker användning
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}