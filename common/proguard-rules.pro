# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.kts.
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
-keep class com.boswelja.smartwatchextensions.common.appmanager.* { *; }
-keep class com.boswelja.smartwatchextensions.common.batterysync.* { *; }
-keep class com.boswelja.smartwatchextensions.common.connection.* { *; }
-keep class com.boswelja.smartwatchextensions.common.dndsync.* { *; }
-keep class com.boswelja.smartwatchextensions.common.preference.* { *; }
-keep class com.boswelja.smartwatchextensions.common.SerializableBitmap { *; }
