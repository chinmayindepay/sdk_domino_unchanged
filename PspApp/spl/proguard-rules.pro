-optimizationpasses 5

-allowaccessmodification
#-mergeinterfacesaggressively
#-overloadaggressively
-keepparameternames
#-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-keepattributes Exceptions,InnerClasses,Signature,Deprecated
-keepattributes SourceFile,LineNumberTable,*Annotation*,EnclosingMethod


-keepclassmembernames class * {
    java.lang.Class class$(java.lang.String);
    java.lang.Class class$(java.lang.String, boolean);
}

#Incaseofproguard
-keepclassmembers class com.indepay.umps.spl.models.** {
  *;
}

-keepclasseswithmembernames class com.indepay.umps.pspsdk.callbacks.** {
    public <methods>;
}

-keepdirectories com.jakewharton.retrofit2
#-keepnames class com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory {}

-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

-keepclassmembers,allowoptimization enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

##########
# Kotlin
##########

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-keep class kotlinx.** { *; }
-keep class kotlin.Metadata { *; }
-keep class org.jetbrains.kotlin.** { *; }
-keep interface org.jetbrains.kotlin.** { *; }
-keep enum org.jetbrains.kotlin.** { *; }
-keep class org.jetbrains.kotlinx.** { *; }
-keep class org.jetbrains.kotlinx.coroutines.** { *; }
-keep class org.jetbrains.kotlin.coroutines.** { *; }
-keep class org.jetbrains.**
-keep class org.jetbrains.annotations.** { *; }
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembers class kotlin.Metadata { public <methods>; }
-keepclassmembers class * {
    static final % *;
    static final java.lang.String *;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

-keepclassmembers class ** {
  @org.jetbrains.annotations.ReadOnly public *;
}

#Retrofit
-keep class kotlin.reflect.** { *; }
-keep class org.jetbrains.anko.** { *; }

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-dontwarn kotlin.reflect.jvm.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# OkHttp3
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

#APPCOMPAT-V7
-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }

-keep public class * extends android.support.v4.view.ActionProvider {
    public <init>(android.content.Context);
}

#SUPPORT_DESIGN
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }

#uk.co.chrisjenx:calligraphy
-keep class uk.co.chrisjenx.calligraphy.* { *; }
-keep class uk.co.chrisjenx.calligraphy.*$* { *; }

#crashlytics
-keepdirectories com.google.firebase
#-keepnames class com.google.firebase.database.FirebaseDatabase {}
-dontwarn com.google.firebase.**

-keep class com.google.firebase.** { *; }
-dontwarn com.crashlytics.**
-keep class com.crashlytics.** { *; }
-keep class com.crashlytics.android.**

# If you are using custom exceptions, add this line so that custom exception types are skipped during obfuscation:
-keep public class * extends java.lang.Exception