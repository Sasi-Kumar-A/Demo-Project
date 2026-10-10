plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "2.0.20"
}

kotlin {
    jvm()
    // androidTarget()
    // iosX64()
    // iosArm64()
    // iosSimulatorArm64()
    js(IR) {
        browser()
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
        }
    }
}
