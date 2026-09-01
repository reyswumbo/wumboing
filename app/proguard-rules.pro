# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn javax.annotation.**

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.wumboing.app.**$$serializer { *; }
-keepclassmembers class com.wumboing.app.** { *** Companion; }
-keepclasseswithmembers class com.wumboing.app.** { kotlinx.serialization.KSerializer serializer(...); }

# jsoup
-keep class org.jsoup.** { *; }
