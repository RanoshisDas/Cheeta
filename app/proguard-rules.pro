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
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# ========== General Android Rules ==========
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep native methods
-keepclassmembers class * {
    native <methods>;
}

# Keep Serializable classes
-keepattributes Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ========== Firebase Rules ==========
# Keep Firebase models
-keep class com.ranoshisdas.app.cheeta.models.** { *; }

# Firebase Authentication
-keep class com.google.firebase.auth.** { *; }
-keepclassmembers class com.google.firebase.auth.** { *; }

# Firebase Firestore
-keep class com.google.firebase.firestore.** { *; }
-keepclassmembers class com.google.firebase.firestore.** { *; }
-keep class com.google.firebase.firestore.proto.** { *; }

# Firebase Common
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
-dontwarn com.google.firebase.**

# Keep fields in Firebase model classes
-keepclassmembers class com.ranoshisdas.app.cheeta.models.** {
    public <fields>;
    public <methods>;
    <init>();
}

# ========== Model Classes (Critical for Firebase) ==========
-keep class com.ranoshisdas.app.cheeta.models.Customer { *; }
-keep class com.ranoshisdas.app.cheeta.models.Item { *; }
-keep class com.ranoshisdas.app.cheeta.models.Bill { *; }
-keep class com.ranoshisdas.app.cheeta.models.BillItem { *; }

# ========== Material Design Components ==========
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**

# Keep Material Design animations
-keep class androidx.transition.** { *; }

# ========== AndroidX Rules ==========
# RecyclerView
-keep class androidx.recyclerview.widget.** { *; }
-keepclassmembers class androidx.recyclerview.widget.** { *; }

# AppCompat
-keep class androidx.appcompat.** { *; }
-dontwarn androidx.appcompat.**

# ConstraintLayout
-keep class androidx.constraintlayout.** { *; }
-dontwarn androidx.constraintlayout.**

# CoordinatorLayout
-keep class androidx.coordinatorlayout.** { *; }

# Core
-keep class androidx.core.** { *; }

# FileProvider
-keep class androidx.core.content.FileProvider { *; }

# ========== iText PDF Library Rules ==========
-keep class com.itextpdf.** { *; }
-keepclassmembers class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# Keep PDF generation classes
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# ========== Utility Classes ==========
-keep class com.ranoshisdas.app.cheeta.utils.** { *; }
-keepclassmembers class com.ranoshisdas.app.cheeta.utils.** {
    public <methods>;
}

# ========== Activity and Fragment Rules ==========
-keep public class * extends android.app.Activity
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.view.View

# Keep Activity methods
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

# ========== View Binding / findViewById ==========
# Keep views that are referenced from layouts
-keepclassmembers class * extends android.app.Activity {
    public <fields>;
}

-keepclassmembers class * extends androidx.appcompat.app.AppCompatActivity {
    public <fields>;
}

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# ========== Adapter Classes ==========
-keep class * extends androidx.recyclerview.widget.RecyclerView$Adapter { *; }
-keep class * extends androidx.recyclerview.widget.RecyclerView$ViewHolder { *; }
-keepclassmembers class * extends androidx.recyclerview.widget.RecyclerView$ViewHolder {
    public <init>(...);
}

# Keep adapter classes specifically
-keep class com.ranoshisdas.app.cheeta.**.adapter.** { *; }
-keep class com.ranoshisdas.app.cheeta.billing.BillAdapter { *; }
-keep class com.ranoshisdas.app.cheeta.billing.BillItemAdapter { *; }
-keep class com.ranoshisdas.app.cheeta.billing.BillItemDetailAdapter { *; }
-keep class com.ranoshisdas.app.cheeta.inventory.ItemAdapter { *; }

# ========== Enum Rules ==========
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ========== Parcelable Rules ==========
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ========== Kotlin Rules (if you add Kotlin later) ==========
-dontwarn kotlin.**
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# ========== Reflection Rules ==========
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

# ========== Gson / JSON Serialization (for Firebase) ==========
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ========== Remove Logging in Release ==========
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# ========== Optimizations ==========
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# ========== Warning Suppressions ==========
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe
-dontwarn com.google.common.**
-dontwarn org.slf4j.**

# ========== Keep R class ==========
-keepclassmembers class **.R$* {
    public static <fields>;
}

# ========== Crashlytics (if you add it later) ==========
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# ========== Custom Application Rules ==========
# Keep all activities, services, receivers
-keep class com.ranoshisdas.app.cheeta.MainActivity { *; }
-keep class com.ranoshisdas.app.cheeta.auth.** { *; }
-keep class com.ranoshisdas.app.cheeta.dashboard.** { *; }
-keep class com.ranoshisdas.app.cheeta.billing.** { *; }
-keep class com.ranoshisdas.app.cheeta.inventory.** { *; }

# Keep constructors
-keepclassmembers class * {
    public <init>(...);
}

# ========== Interface and Listener Rules ==========
-keep interface * {
    <methods>;
}

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep onClick methods referenced from XML
-keepclassmembers class * {
    public void onClick(android.view.View);
}

# ========== Additional Safety Rules ==========
# Keep everything in the main package just to be safe
-keep class com.ranoshisdas.app.cheeta.** { *; }

# Print mapping for debugging
-printmapping mapping.txt
-printseeds seeds.txt
-printusage usage.txt