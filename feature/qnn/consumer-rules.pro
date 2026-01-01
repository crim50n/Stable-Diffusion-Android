# Keep JNI methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep QNN Bridge
-keep class dev.minios.pdaiv1.feature.qnn.jni.QnnBridge { *; }
-keep class dev.minios.pdaiv1.feature.qnn.jni.QnnBridge$* { *; }
