# Maker Playground

Maker Playground is an integrated development platform for creating electronics project, developing device's firmware, getting your circuit designed and programming your micro-controller board.

## Quickstart

See how Maker Playground works on [youtube](https://youtu.be/H7rkG30609k) and follow our [documentation](https://docs.makerplayground.io/th) to start building your own project.

## Installation

Latest releases for major operating systems can be downloaded on our [website](https://www.makerplayground.io/#installer-section). Detailed instructions for installation can be found in our [documentation](https://docs.makerplayground.io/th). 

## Build Instruction

This instruction is for those who prefer to build Maker Playground from source or to contribute to this project. Most users should download and install the pre-built version of Maker Playground available on our [website](https://www.makerplayground.io/#installer-section).

### Prerequisite

OS            | JDK Version     | Gradle's Build Script
--------------|-----------------|------------------------------------
Windows (x86) | JDK11-14*       | build-jdk-fx.gradle**
Windows (x64) | JDK11 or higher | build.gradle or build-jdk-fx.gradle
macOS (x64)   | JDK11 or higher | build.gradle or build-jdk-fx.gradle
macOS (arm64) | JDK11 or higher | build-jdk-fx.gradle**
Linux (x64)   | JDK11 or higher | build.gradle or build-jdk-fx.gradle

\* JDK15 and 16 is not supported on Windows 32-bit due to this jpackage [bug](https://bugs.openjdk.java.net/browse/JDK-8258755) which will be fixed in JDK17

** Tested on Azul JDK FX and Bellsoft Liberica Full JDK

### Using command line

1. Clone the project

    ```
    git clone https://github.com/InGarage/MakerPlayGround.git
    git submodule update --init --recursive 
    ```

2. Select the appropriate gradle build script. Use *build.gradle* for standard OpenJDK or *build-jdk-fx.gradle* for OpenJDK with OpenJFX included by uncomment the following line in *settings.gradle*

    ```gradle
    rootProject.name = 'makerplayground'
    // rootProject.buildFileName = 'build-jdk-fx.gradle'
    ```

3. Build and run using gradle wrapper  

    ```
    chmod +x gradlew
    ./gradlew run
    ```

### Using IDEs

Import this project as a Gradle project using the instruction provide by your IDEs such as [this guide](https://www.jetbrains.com/help/idea/gradle.html#gradle_import_project_start) for IntelliJ IDEA