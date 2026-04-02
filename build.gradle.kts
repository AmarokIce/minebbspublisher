plugins {
  kotlin("jvm") version "2.3.20"
  `kotlin-dsl`
  `java-gradle-plugin`
  `maven-publish`
}

group = "club.someoneice.mbp"
version = "1.0"

gradlePlugin {
  plugins {
    create("minebbs-publisher") {
      id = "minebbs-publisher"
      implementationClass = "club.someoneice.mbp.MBPPlugin"
    }
  }
}

repositories {
  mavenCentral()
}

dependencies {
  compileOnly("com.google.code.findbugs:jsr305:3.0.2")

  implementation("club.snowlyicewolf:amarok-json-for-java:1.7.10")
  implementation("com.squareup.okhttp3:okhttp:4.12.0")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  compilerOptions {
    apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_1)
    languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_1)
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

apply(from = "publishing.gradle")