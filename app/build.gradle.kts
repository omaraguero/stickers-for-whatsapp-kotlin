import com.android.build.api.dsl.Packaging
import java.util.Properties


plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    //id("kotlin-android-extensions")
}

android {
    namespace = "com.roa.cswstickers"
    compileSdk = 34

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }


    fun Packaging.() {
        resources {
            excludes.add("**/*.webp")
            excludes.add("lib/**/libnative-imagetranscoder.so")
            excludes.add("lib/**/libnative-filters.so")
        }
    }

    defaultConfig {
        applicationId = "com.roa.cswstickers"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


        val contentProviderAuthority = "$applicationId.whatsapp_api.StickerContentProvider"
        manifestPlaceholders["contentProviderAuthority"] = contentProviderAuthority

        buildConfigField("String", "CONTENT_PROVIDER_AUTHORITY", "\"${contentProviderAuthority}\"")

    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

tasks.register("checkDebug") {
    doLast {
        println("checkDebug")
        if (android.defaultConfig.applicationId?.startsWith("com.whatsapp") == true) {
            throw GradleException("applicationId in defaultConfig cannot start with com.whatsapp, please change your applicationId in app/build.gradle")
        }
        checkApplicationIdInDebug()
    }
}

fun checkApplicationIdInDebug() {
    //revisar Properties
    val properties = Properties()
    properties.load(project.rootProject.file("local.properties").inputStream())
    val ignoreApplicationIdCheck = properties.getProperty("ignoreApplicationIdCheck")
    if (ignoreApplicationIdCheck == null) {
        // Handle accordingly
    } else {
        println("application id check ignored")
    }
}

dependencies {
    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")

    //
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.0")


    //noinspection GradleCompatible
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.5.2")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    //
    implementation("com.google.android.material:material:1.11.0")



    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    val frescoVersion = "2.5.0"
    implementation("com.facebook.fresco:fresco:$frescoVersion")
    implementation("com.facebook.fresco:webpsupport:$frescoVersion")
    implementation("com.facebook.fresco:animated-webp:$frescoVersion")
    implementation("com.facebook.fresco:animated-base:$frescoVersion")


    implementation("io.github.sangcomz:fishbun:1.1.1")
    implementation("com.github.bumptech.glide:glide:4.12.0")


    //
    implementation ("io.coil-kt:coil:0.11.0")



    implementation("com.alexvasilkov:gesture-views:2.8.3")

    implementation("com.vanniktech:android-image-cropper:4.5.0")


    //
    implementation("com.github.jkwiecien:EasyImage:Tag")


    //
    implementation("com.github.duanhong169:checkerboarddrawable:1.0.2")

    implementation("com.github.bumptech.glide:glide:4.5.0")

    //algo me causa error con esto
    implementation("com.airbnb.android:lottie:2.7.0")

    implementation("com.google.code.gson:gson:2.10.1")


    implementation("com.github.duanhong169:checkerboarddrawable:1.0.2")

    //
    implementation("com.github.AppIntro:AppIntro:4.2.3")


    implementation("com.github.jkwiecien:EasyImage:1.3.1")

    //
    //implementation("com.github.florent37:depth:1.0.0")

    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.0"))

    implementation("com.jakewharton.timber:timber:5.0.1")
}

/*
repositories {
    mavenCentral()
    maven { url = uri("https://www.jitpack.io" ) }
}

/*
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

}
*/
