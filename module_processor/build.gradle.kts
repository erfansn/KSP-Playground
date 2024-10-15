plugins {
  alias(libs.plugins.jetbrains.kotlin.jvm)
}

kotlin {
  jvmToolchain(21)
}

dependencies {
  implementation(project(":annotation"))
  implementation(libs.symbol.processing.api)
}