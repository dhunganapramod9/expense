# Windows JDK setup

The project pins Gradle 9.6.0, Android Gradle Plugin 9.5.0, and Java 25 daemon
criteria in `gradle/gradle-daemon-jvm.properties`. Use a **complete JDK 25** with
`java.exe`, `javac.exe`, `jlink.exe`, and `jmods/java.base.jmod`.
The Java 11 `compileOptions` control generated app bytecode; they do not select
the JDK that runs Gradle. The Kotlin version printed by `gradlew --version` is
Gradle's embedded Kotlin, not the app's Compose plugin version (2.2.10).

## Why the missing-jlink error happens

A path under `.vscode/extensions/redhat.java-.../jre/` is an editor-managed Java
runtime. The reported path lacks `jlink.exe`, which the failing build operation
needs. A Java version match alone does not establish a complete JDK installation.

The checked-in files contain no VS Code JRE path. The error establishes the
runtime used by the failing operation, but does not identify which Windows-local
setting or cached transform selected it. The launcher JVM, daemon JVM, IDE SDK,
and compilation toolchains can differ. Daemon JVM criteria take precedence over
`JAVA_HOME` and `org.gradle.java.home` for selecting the daemon. Changing an IDE
criteria dropdown to 17 is not enough if the effective project criteria still
require 25, another toolchain is selected, or cached configuration retains a path.

Project settings now disable automatic JDK discovery/download and use the JDK
exported through `JAVA_HOME`. The current Gradle JVM is also an implicit candidate;
launch Gradle and configure Android Studio with the same complete JDK.

## 1. Find and check a complete JDK

In PowerShell, list common installation locations (missing folders are harmless):

```powershell
Get-ChildItem 'C:\Program Files\Eclipse Adoptium', 'C:\Program Files\Java', "$env:USERPROFILE\.jdks" -Directory -ErrorAction SilentlyContinue
Get-Command java -All
```

Select your actual full JDK 25 installation, not the `bin` folder or a VS Code
extension folder. The following path is an example; replace it with an existing
folder from your machine:

```powershell
$jdk = 'C:\Program Files\Eclipse Adoptium\YOUR-JDK-25-FOLDER'
Test-Path "$jdk\bin\java.exe"
Test-Path "$jdk\bin\javac.exe"
Test-Path "$jdk\bin\jlink.exe"
Test-Path "$jdk\jmods\java.base.jmod"
& "$jdk\bin\java.exe" -version
& "$jdk\bin\javac.exe" -version
& "$jdk\bin\jlink.exe" --version
```

All four checks must return True, and the tools must report Java 25. If necessary,
install the official Eclipse Temurin 25 **JDK**, Windows x64 package. Do not copy
`jlink.exe` from another installation into the editor runtime.

## 2. Align Windows and Android Studio

1. Set your Windows **user** `JAVA_HOME` variable to the verified JDK root through
   “Edit environment variables for your account.” Put `%JAVA_HOME%\bin` ahead of
   obsolete Java entries in your user Path. Avoid removing unrelated editor settings.
2. Close and reopen Android Studio and its terminals so they inherit the change.
3. Open `P:\mobile_app_development\ExpenseTracker`.
4. In **File > Settings > Build, Execution, Deployment > Build Tools > Gradle**,
   use the project's Gradle wrapper and select the verified full JDK root for the
   Gradle JVM, or JAVA_HOME if that option resolves to the same root. If your Studio
   version shows **Gradle JVM criteria**, keep it at **25**, matching the checked-in
   daemon file. Do not select a `.vscode/.../jre` installation or change it to 17.
5. Check **File > Project Structure > SDK Location** and SDK Manager. The Android
   SDK should resolve to `C:\Users\pram9\AppData\Local\Android\Sdk`. Install
   Android SDK Platform 37 and Build Tools 36.0.0. Keep your API 36 emulator image;
   the compile SDK and emulator API do not need to match.
6. If `.gradle/config.properties` contains `java.home` pointing to VS Code, use the
   IDE's Gradle JVM selection to update it to the verified JDK. This file is local
   and must not be committed with a machine-specific path.
7. Inspect project and user Gradle properties for conflicting overrides. The user
   file is normally `%USERPROFILE%\.gradle\gradle.properties`, or under
   `%GRADLE_USER_HOME%` if that variable is set. Back up the file, then remove or
   correct obsolete `org.gradle.java.home` and `org.gradle.java.installations.*`
   entries referencing the VS Code JRE or overriding the project discovery policy.
   Do not delete other settings or credentials. Also check any Java-path overrides
   in `GRADLE_OPTS`, `JAVA_TOOL_OPTIONS`, and `JDK_JAVA_OPTIONS` locally.

Do not commit your Windows JDK or SDK paths into shared Gradle files. Do not remove
VS Code's runtime files; simply stop selecting them for this Android build.

## 3. Stop daemons and build

From the project's PowerShell terminal, after applying the Gradle property changes:

```powershell
Set-Location 'P:\mobile_app_development\ExpenseTracker'
.\scripts\build-debug.ps1 -JdkHome $jdk
```

The helper validates the installation, sets session-local `JAVA_HOME`, stops
Gradle, prints JVM/toolchain information, and builds first without configuration
cache and then with it. It exits with an error if any command fails. It does not
change the application's source or Windows registry. If your PowerShell policy
blocks scripts, run the equivalent commands below rather than weakening the policy.

```powershell
$env:JAVA_HOME = $jdk
$env:Path = "$jdk\bin;$env:Path"
.\gradlew.bat --stop
.\gradlew.bat --version
.\gradlew.bat -q javaToolchains --no-configuration-cache
.\gradlew.bat :app:assembleDebug --no-configuration-cache --stacktrace --info *> build-jdk-diagnostic.log
# Check $LASTEXITCODE; read the complete log if it is not zero.
.\gradlew.bat :app:assembleDebug --configuration-cache --stacktrace
```

The launcher and selected daemon/toolchain must resolve to the complete JDK 25.
Do not infer the actual daemon installation only from a “Compatible with Java 25”
criteria line; check `javaToolchains` and the daemon startup path in `--info` output.
The APK is `app\build\outputs\apk\debug\app-debug.apk`.

## Configuration-cache troubleshooting

The missing `jlink` message alone does not prove a configuration-cache defect. Keep
configuration caching enabled in the project and compare the two builds above.
If the uncached build succeeds but the cached build fails, keep the complete
exception and configuration-cache report. With Android Studio closed and daemons
stopped, rename the project-local `.gradle\configuration-cache` directory to a
backup name, if present, and retry. This preserves it for diagnosis. Do not delete
source files or clear every global Gradle cache as a first step.

If both modes still name the VS Code runtime, a JVM/toolchain override remains.
If neither does, follow the next concrete error in the complete log instead of
assuming that all later failures have the same cause.

## Run on Medium Phone API 36

Once Gradle Sync and assembleDebug succeed, start **Medium Phone API 36** in Device
Manager, select it beside the app run configuration, and click Run. The app's
minimum SDK is 24, so an API 36 emulator meets that minimum even though the project
compiles and targets API 37. Actual emulator launch and UI behavior must be checked
on your Windows machine; a successful cloud build does not verify that step.

## Verification of this change

In the Linux development environment, the selected toolchain was Oracle JDK
25.0.5.0.3 (`/workspace/toolchains/jdk-25.0.5.0.3`), with working `java`, `javac`,
and `jlink`. Gradle's `javaToolchains` reported `Is JDK: true`, with automatic
discovery and downloads disabled.

- `--stop` completed; no Gradle daemons were running.
- `--version` reported Gradle 9.6.0 and the Oracle Java 25 launcher.
- `:app:clean :app:assembleDebug --no-configuration-cache --info --stacktrace`
  succeeded, executing 37 tasks.
- `:app:assembleDebug --configuration-cache --stacktrace` succeeded and stored
  a cache entry; a second invocation succeeded and reused it.
- A new debug APK was produced. Application source, dependencies, and SDK versions
  were unchanged.

The Windows PowerShell helper has not been executed here. Your installed Windows
JDKs, the precise source of the VS Code runtime selection, any separate Windows
configuration-cache error, and the API 36 emulator remain local verification steps.
