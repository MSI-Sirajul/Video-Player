#  অ্যাগ্রেসিভ কোড শ্রিংকিং

# বেসিক অপশন
-dontobfuscate
-dontoptimize
-verbose

#  সব অপ্রয়োজনীয় লোগ বাদ
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

#  Android ক্লাস রাখা
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

# Native মেথড রাখা
-keepclasseswithmembernames class * {
    native <methods>;
}

# Parcelable রাখা
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

#  View এবং ক্লিক লিসেনার রাখা
-keepclassmembers class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(***);
    *** get*();
}

-keepclassmembers class * {
    public void onClick(android.view.View);
}

#  Resource ক্লাস রাখা
-keepclassmembers class **.R$* {
    public static <fields>;
}

#  Reflection ব্যবহার করা ক্লাস
-keepattributes Signature
-keepattributes *Annotation*

# কোনো AndroidX-specific রুল নাই
# কোনো থার্ড পার্টি লাইব্রেরি রুল নাই
# কোনো Google/Firebase রুল নাই

# বাকি সব বাদ!
-keep class !com.yourcompany.purejavaapp.** { *; }
-dontwarn **