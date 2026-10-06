# Legacy compatibility file.
# AGP 9.4 release rules live in src/main/keepRules/astha-release.keep.

# Huawei HMS/Safety Detect (becomes relevant when SDK is added)
-keepattributes *Annotation*,Exceptions,InnerClasses,Signature
-keep class com.huawei.hms.** { *; }
-keep class com.huawei.updatesdk.** { *; }
