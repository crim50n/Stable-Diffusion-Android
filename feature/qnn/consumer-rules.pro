# Keep JNI methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep QNN Bridge
-keep class com.shifthackz.aisdv1.feature.qnn.jni.QnnBridge { *; }
-keep class com.shifthackz.aisdv1.feature.qnn.jni.QnnBridge$* { *; }
