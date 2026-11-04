param(
    [Parameter(Mandatory = $true)]
    [string]$JdkHome
)

$ErrorActionPreference = 'Stop'
$JdkHome = (Resolve-Path $JdkHome).Path
foreach ($tool in @('java', 'javac', 'jlink')) {
    if (-not (Test-Path (Join-Path $JdkHome "bin\$tool.exe"))) {
        throw "Missing bin\$tool.exe. Select a complete JDK 25, not a VS Code extension runtime."
    }
}
if (-not (Test-Path (Join-Path $JdkHome 'jmods\java.base.jmod'))) {
    throw 'The selected installation has no jmods\java.base.jmod. Install a complete JDK 25.'
}
$jlink = & (Join-Path $JdkHome 'bin\jlink.exe') --version
if ($LASTEXITCODE -ne 0 -or "$jlink" -notmatch '^25(?:\.|$)') {
    throw "This project requires JDK 25; jlink reported: $jlink"
}

# These settings apply only to this PowerShell session and its child processes.
$env:JAVA_HOME = $JdkHome
$env:Path = "$JdkHome\bin;$env:Path"
$projectRoot = Split-Path $PSScriptRoot -Parent
Push-Location $projectRoot
try {
    $selection = @(
        '-Dorg.gradle.java.installations.auto-detect=false',
        '-Dorg.gradle.java.installations.auto-download=false',
        '-Dorg.gradle.java.installations.fromEnv=JAVA_HOME',
        "-Dorg.gradle.java.installations.paths=$JdkHome"
    )
    & .\gradlew.bat --stop
    if ($LASTEXITCODE -ne 0) { throw 'Stopping Gradle failed.' }
    & .\gradlew.bat --version
    if ($LASTEXITCODE -ne 0) { throw 'Gradle JVM verification failed.' }
    & .\gradlew.bat @selection -q javaToolchains --no-configuration-cache
    if ($LASTEXITCODE -ne 0) { throw 'Toolchain inspection failed.' }
    & .\gradlew.bat @selection :app:assembleDebug --no-configuration-cache --stacktrace
    if ($LASTEXITCODE -ne 0) { throw 'Debug build failed. Inspect the full Gradle error above.' }
    # Check the normal configuration-cache workflow after the diagnostic build.
    & .\gradlew.bat @selection :app:assembleDebug --configuration-cache --stacktrace
    if ($LASTEXITCODE -ne 0) { throw 'Configuration-cache build failed. Inspect its error above.' }
    Write-Host 'Debug APK: app\build\outputs\apk\debug\app-debug.apk'
} finally {
    Pop-Location
}
