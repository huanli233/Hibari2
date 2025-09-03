@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    targets.all {
        compilations.all {
            compileTaskProvider.configure{
                compilerOptions {
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }
            }
        }
    }
    androidTarget {
        configurations.all {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_1_8)
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":shared:runtime"))
                api(libs.androidx.core.ktx)
                api(libs.androidx.appcompat)
                api(libs.compose.runtime)
            }
        }
        val androidMain by getting {
            dependencies {
                api(libs.compose.ui)
                api(libs.material)
            }
        }
    }
}

android {
    namespace = "com.huanli233.hibari2.foundation"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}