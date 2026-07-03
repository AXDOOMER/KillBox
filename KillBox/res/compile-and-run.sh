#!/bin/bash

# Set the current working directory to the directory of the script
cd $(dirname "$0")

LWJGL_VERSION=3.3.6
LWJGL_DIR=lwjgl3

# Build classpath for compilation (no native JARs needed at compile time)
CP_COMPILE="$LWJGL_DIR/lwjgl-$LWJGL_VERSION.jar:\
$LWJGL_DIR/lwjgl-glfw-$LWJGL_VERSION.jar:\
$LWJGL_DIR/lwjgl-openal-$LWJGL_VERSION.jar:\
$LWJGL_DIR/lwjgl-opengl-$LWJGL_VERSION.jar"

# Build classpath for running (includes native JARs so LWJGL auto-extracts them)
NATIVE_SUFFIX="natives-macos-arm64"
CP_RUN="$CP_COMPILE:\
$LWJGL_DIR/lwjgl-$LWJGL_VERSION-$NATIVE_SUFFIX.jar:\
$LWJGL_DIR/lwjgl-glfw-$LWJGL_VERSION-$NATIVE_SUFFIX.jar:\
$LWJGL_DIR/lwjgl-openal-$LWJGL_VERSION-$NATIVE_SUFFIX.jar:\
$LWJGL_DIR/lwjgl-opengl-$LWJGL_VERSION-$NATIVE_SUFFIX.jar"

# macOS requires -XstartOnFirstThread for GLFW to run on the main thread
JAVA_OPTS="-XstartOnFirstThread"

# Compile the source files and execute only if successful
if javac ../src/*.java -cp "$CP_COMPILE"; then
	cd ..
	java $JAVA_OPTS -cp "res/$CP_RUN:src/" Game $@
fi

