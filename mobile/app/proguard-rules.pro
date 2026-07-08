# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# --- Reeler rules ---

# java-youtube-downloader pulls in fastjson, whose desktop/server codepaths
# reference classes that don't exist on Android. Safe to ignore.
-dontwarn java.awt.**
-dontwarn javax.money.**
-dontwarn javax.ws.rs.**
-dontwarn org.glassfish.jersey.**
-dontwarn org.javamoney.moneta.**
-dontwarn org.joda.time.**
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn springfox.documentation.spring.web.json.Json

# fastjson deserializes YouTube models reflectively.
-keep class com.github.kiulian.downloader.model.** { *; }
-keep class com.alibaba.fastjson.** { *; }

# Keep line numbers for readable crash reports.
-keepattributes SourceFile,LineNumberTable
-dontwarn javax.servlet.**
-dontwarn org.springframework.**
