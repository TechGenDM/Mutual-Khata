@echo off
setlocal
set APP_HOME=%~dp0
set GRADLE_VERSION=8.9
if "%GRADLE_USER_HOME%"=="" set GRADLE_USER_HOME=%USERPROFILE%\.gradle
set DIST_ROOT=%GRADLE_USER_HOME%\mutual-khata-distributions
set GRADLE_DIR=%DIST_ROOT%\gradle-%GRADLE_VERSION%
if not exist "%GRADLE_DIR%\bin\gradle.bat" (
  if not exist "%DIST_ROOT%" mkdir "%DIST_ROOT%"
  echo Downloading Gradle %GRADLE_VERSION% for the first build...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; $zip=Join-Path '%DIST_ROOT%' 'gradle-%GRADLE_VERSION%-bin.zip'; Invoke-WebRequest 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile $zip; Expand-Archive -Path $zip -DestinationPath '%DIST_ROOT%' -Force; Remove-Item $zip"
  if errorlevel 1 exit /b 1
)
call "%GRADLE_DIR%\bin\gradle.bat" --project-dir "%APP_HOME%" %*
endlocal
