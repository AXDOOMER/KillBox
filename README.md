# KillBox

![Top Language](https://img.shields.io/github/languages/top/axdoomer/killbox.svg?style=flat)
![Code Size](https://img.shields.io/github/languages/code-size/axdoomer/killbox.svg?style=flat)
![License](https://img.shields.io/github/license/axdoomer/killbox.svg?style=flat&logo=gnu)

A multiplayer FPS game that I made with friends during college (2015) using [LWJGL](http://lwjgl.org/). It is distributed under the GNU GPL v3. 

![alt tag](https://cloud.githubusercontent.com/assets/6194072/15656520/3929f0be-2676-11e6-80ac-94b115237652.jpg)

System requirements:
* To **run the pre-built JAR**: Java 11 or later (JRE is sufficient).
* To **build from source**: Java 17–26 (Gradle 9.x requirement; JDK required).
* Your computer must support OpenGL.
* LWJGL 3 is fetched automatically by Gradle — no manual download needed.

## Building and running

The project uses Gradle. The wrapper script (`gradlew`) is included, so no Gradle installation is required.

### Linux and macOS

From the `KillBox/` directory (the one that contains `build.gradle`):

```sh
./gradlew run
```

On macOS, GLFW requires the main thread, so the JVM flag `-XstartOnFirstThread` is applied automatically by the build script.

### Windows

```bat
gradlew.bat run
```

### Pre-built JAR

Download `KillBox.jar` from the latest CI build and run it:

```sh
# Linux / Windows
java -jar KillBox.jar

# macOS
java -XstartOnFirstThread -jar KillBox.jar
```

# Screenshots
###### KillBox running on Ubuntu MATE (map: Citadel)
![citadel](https://cloud.githubusercontent.com/assets/6194072/19622332/915b8718-9875-11e6-84cc-75e7d3226328.png)
###### KillBox running on Windows 7 (map: Demo)
![demo](https://cloud.githubusercontent.com/assets/6194072/19622333/915fa00a-9875-11e6-8631-80253a8131ce.png)
###### KillBox running on Ubuntu (map: Houses)
![houses](https://cloud.githubusercontent.com/assets/6194072/19622331/915b046e-9875-11e6-8b2c-5c2821c6d7d9.png)
###### KillBox running on Debian (map: Manor)
![manor](https://cloud.githubusercontent.com/assets/6194072/19622334/9162b7ea-9875-11e6-9a2d-5553f2935aa1.png)

