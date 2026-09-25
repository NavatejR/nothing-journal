# ---- Journal -------------------------------------------------------------
# Room entities are accessed via reflection-free generated code, but keep
# them anyway so R8 never renames the @Entity columns out of sync with DAOs.
-keep class com.nothingjournal.data.local.** { *; }

# ---- Bundled AI runtimes -------------------------------------------------
# MediaPipe / LiteRT GenAI loads its graph through JNI with name lookups.
-keep class com.google.mediapipe.** { *; }
-keep class com.google.protobuf.** { *; }
-dontwarn com.google.mediapipe.**

# whisper.cpp is reached through our JNI bridge; keep the bridge's native
# method names intact.
-keepclasseswithmembernames class com.nothingjournal.ai.WhisperJni {
    native <methods>;
}

# Kotlin coroutines
-dontwarn kotlinx.coroutines.**
