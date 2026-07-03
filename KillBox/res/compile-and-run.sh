#!/bin/bash
# Build and run KillBox using Gradle.
# Gradle downloads LWJGL automatically from Maven Central on first run.
# On macOS, -XstartOnFirstThread is applied automatically via build.gradle.

cd "$(dirname "$0")/.."   # navigate to KillBox/ (where build.gradle lives)
./gradlew run "$@"

