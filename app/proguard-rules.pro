# Gson reads WiseKoin's API DTO fields reflectively. Keep their names and generic
# signatures stable while allowing R8 to optimize the rest of the application.
-keepattributes Signature,*Annotation*
-keep class com.wisekoin.data.model.** { *; }

# Retrofit inspects service method and parameter annotations at runtime.
-keepclasseswithmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Preserve source and line data for actionable, de-obfuscated crash reports.
-keepattributes SourceFile,LineNumberTable
