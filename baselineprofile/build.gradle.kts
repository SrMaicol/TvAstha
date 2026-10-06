plugins {
    id("com.android.test")
    id("androidx.baselineprofile")
}
android {
    namespace = "com.asthatv.baselineprofile"
    compileSdk = 37
    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true
    defaultConfig { minSdk = 24 }
}
baselineProfile {
    useConnectedDevices = true
}
dependencies {
    implementation("androidx.test.ext:junit:1.3.0")
    implementation("androidx.test.uiautomator:uiautomator:2.3.0")
    implementation("androidx.benchmark:benchmark-macro-junit4:1.4.1")
}
