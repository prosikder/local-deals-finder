# ProGuard and R8 Rules for Local Deals Finder

# Keep Room Database, DAOs, Entities, and TypeConverters
-keep class androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class com.localdeals.finder.data.local.entity.** { *; }
-keep class com.localdeals.finder.data.local.converter.** { *; }
-keep class com.localdeals.finder.data.local.dao.** { *; }

# Keep Domain Models (for serialization & state retention)
-keep class com.localdeals.finder.domain.model.** { *; }

# Keep Hilt and Dagger
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep class dagger.hilt.** { *; }

# Keep WorkManager workers
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class com.localdeals.finder.worker.** { *; }

# Keep Kotlinx Coroutines
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Keep Compose Runtime
-keep class androidx.compose.runtime.** { *; }

# Strip logging in release builds (AppLogger handles safe release logging)
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}

# Keep line numbers and source file names for R8 crash de-obfuscation
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
