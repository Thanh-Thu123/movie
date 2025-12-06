plugins {
    alias(libs.plugins.android.application) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false // Same for this
}
tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}