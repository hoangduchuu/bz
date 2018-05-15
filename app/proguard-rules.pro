# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/apple/Softwares/adt-bundle-mac-x86_64-20140702/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontpreverify

-keep class com.ping.android.model.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.quickblox.core.account.model.** { *; }

##---------------End: proguard configuration for Gson  ----------

##---------------Begin: proguard configuration for quickblox  ----------

#quickblox core module
-keep class com.quickblox.auth.parsers.** { *; }
-keep class com.quickblox.auth.model.** { *; }
-keep class com.quickblox.core.parser.** { *; }
-keep class com.quickblox.core.model.** { *; }
-keep class com.quickblox.core.server.** { *; }
-keep class com.quickblox.core.rest.** { *; }
-keep class com.quickblox.core.error.** { *; }
-keep class com.quickblox.core.Query { *; }

-dontwarn org.jivesoftware.smackx.**

-keep class org.jivesoftware.smack.** { *; }

-keep class com.quickblox.messages.model.** { *; }

#quickblox videochat-webrtc module
-keep class org.webrtc.** { *; }

##---------------End: proguard configuration for quickblox  ----------

-keepattributes *Annotation*

# JavaCV
-keep @org.bytedeco.javacpp.annotation interface * {
    *;
}

-keep @org.bytedeco.javacpp.annotation.Platform public class *

-keepclasseswithmembernames class * {
    @org.bytedeco.* <fields>;
}

-keepclasseswithmembernames class * {
    @org.bytedeco.* <methods>;
}

-keepattributes EnclosingMethod
-keep @interface org.bytedeco.javacpp.annotation.*,javax.inject.*

-keepattributes *Annotation*, Exceptions, Signature, Deprecated, SourceFile, SourceDir, LineNumberTable, LocalVariableTable, LocalVariableTypeTable, Synthetic, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations, RuntimeVisibleParameterAnnotations, RuntimeInvisibleParameterAnnotations, AnnotationDefault, InnerClasses
-keep class org.bytedeco.javacpp.** {*;}
-dontwarn java.awt.**
-dontwarn org.bytedeco.javacv.**
-dontwarn org.bytedeco.javacpp.**

# end javacv

# crashlytic
#-keepattributes SourceFile,LineNumberTable
# end crashlytic

# Firebase Auth
## Twitter and Facebook are optional
#-dontwarn com.facebook.**
#-dontwarn com.twitter.**
#
## Don't note a bunch of dynamically referenced classes
#-dontnote com.google.**
#-dontnote com.facebook.**
#-dontnote com.twitter.**
#-dontnote com.squareup.okhttp.**
#-dontnote okhttp3.internal.**
#
## Recommended flags for Firebase Auth
#-keepattributes Signature
#-keepattributes *Annotation*
#
## Retrofit config
#-dontnote retrofit2.Platform
#-dontwarn retrofit2.** # Also keeps Twitter at bay as long as they keep using Retrofit
#-dontwarn okio.**
#-keepattributes Exceptions
#
## end Firebase Auth

-dontwarn org.opencv.highgui.**


##---------------End: proguard configuration ----------