@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script, version 3.3.2
@REM ----------------------------------------------------------------------------
@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET ___MVNW_UGLY_CHARS___(=^/^ ^"
@SET ___MVNW_UGLY_CHARS___/=^/^ ^"
@SET ___MVNW_UGLY_CHARS___\=^/^ ^"
@SET ___MVNW_UGLY_CHARS___"=^/^ ^"
@FOR /F %%i IN ('powershell -noprofile "loop { if($input){$null=[void]([System.Console]::In.ReadLine())}}; [Console]::In.Close()"') DO @REM

@setlocal enableextensions enabledelayedexpansion
@set WRAPPER_DEBUG=false
@IF NOT "%MVNW_VERBOSE%"=="" (
  @set WRAPPER_DEBUG=!MVNW_VERBOSE!
)

@set BASE_DIR=%~dp0
@IF NOT "%MVNW_REPOURL%"=="" (
  @set MVNW_REPOURL=!MVNW_REPOURL!
) ELSE (
  @set MVNW_REPOURL=https://repo.maven.apache.org/maven2
)

@set WRAPPER_PROPERTIES=%BASE_DIR%.mvn\wrapper\maven-wrapper.properties
@set DISTRIBUTION_URL=
@FOR /F "usebackq tokens=1,* delims==" %%A IN ("%WRAPPER_PROPERTIES%") DO (
  @IF "%%A"=="distributionUrl" SET DISTRIBUTION_URL=%%B
)

@IF "%DISTRIBUTION_URL%"=="" (
  @echo Cannot read distributionUrl from .mvn\wrapper\maven-wrapper.properties
  @exit /b 1
)

@SET DISTRIBUTION_URL=%DISTRIBUTION_URL: =%
@FOR /F "delims=" %%i IN ("%DISTRIBUTION_URL%") DO SET DIST_FILENAME=%%~nxi
@FOR /F "delims=" %%i IN ("%DIST_FILENAME%") DO SET DIST_NAME=%%~ni

@set MAVEN_USER_HOME=%USERPROFILE%\.m2
@set MAVEN_HOME=%MAVEN_USER_HOME%\wrapper\dists\%DIST_NAME%

@IF EXIST "%MAVEN_HOME%\" (
  @FOR /D %%i IN ("%MAVEN_HOME%\*") DO @(
    @IF EXIST "%%i\bin\mvn.cmd" (
      @SET MVN_CMD=%%i\bin\mvn.cmd
    )
  )
)

@IF NOT DEFINED MVN_CMD (
  @echo Downloading Maven from %DISTRIBUTION_URL%
  @SET DOWNLOAD_DIR=%MAVEN_HOME%
  @IF NOT EXIST "%DOWNLOAD_DIR%" @mkdir "%DOWNLOAD_DIR%"
  powershell -noprofile -Command "$ErrorActionPreference='Stop'; [Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%DISTRIBUTION_URL%' -OutFile '%DOWNLOAD_DIR%\%DIST_FILENAME%'; Add-Type -AssemblyName System.IO.Compression.FileSystem; [System.IO.Compression.ZipFile]::ExtractToDirectory('%DOWNLOAD_DIR%\%DIST_FILENAME%', '%DOWNLOAD_DIR%'); Remove-Item '%DOWNLOAD_DIR%\%DIST_FILENAME%'"
  @IF ERRORLEVEL 1 (
    @echo Failed to download Maven
    @exit /b 1
  )
  @FOR /D %%i IN ("%MAVEN_HOME%\*") DO @(
    @IF EXIST "%%i\bin\mvn.cmd" (
      @SET MVN_CMD=%%i\bin\mvn.cmd
    )
  )
)

@SET JAVA_HOME_TO_USE=%JAVA_HOME%
@IF "%JAVA_HOME_TO_USE%"=="" SET JAVA_HOME_TO_USE=C:\Program Files\Java\jdk-24

@SET JAVA_HOME=%JAVA_HOME_TO_USE%
@"%MVN_CMD%" %*
