@echo off
rem 
rem Licensed to the Apache Software Foundation (ASF) under one or more
rem contributor license agreements.  See the NOTICE file distributed with
rem this work for additional information regarding copyright ownership.
rem The ASF licenses this file to You under the Apache License, Version 2.0
rem (the "License"); you may not use this file except in compliance with
rem the License.  You may obtain a copy of the License at
rem
rem     http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.

rem Batch script for defining the ProcrunService (JVM and Java versions)

rem Copy this file and ProcrunService.jar into the same directory as prunsrv (or adjust the paths below)

setlocal
rem The service names (make sure they does not clash with an existing service)
set SERVICE_NAME=${artifactId}
set SERVICE_DISPLAY_NAME="${project.name}"
set SERVICE_DESCRIPTION="${project.description}"

rem my location
set BIN_PATH=%~dp0

rem location of Prunsrv
set PATH_PRUNSRV=%BIN_PATH%
set BASE_DIR=%PATH_PRUNSRV%\..
set LOG_DIR=%BASE_DIR%\logs
rem location of jarfile
set PATH_JAR=%BIN_PATH%

rem Allow prunsrv to be overridden
if "%PRUNSRV%" == "" set PRUNSRV=%PATH_PRUNSRV%prunsrv
rem Install the 2 services

echo Installing %SERVICE_NAME%
%PRUNSRV% //IS//%SERVICE_NAME% --Jvm=auto --StdOutput auto --StdError auto ^
--Classpath=%PATH_JAR%${artifactId}.jar ^
--StartMode=jvm --StartClass=nl.denhaag.twb.layer7.Layer7Monitor --StartMethod=start ^
 --StopMode=jvm  --StopClass=nl.denhaag.twb.layer7.Layer7Monitor  --StopMethod=stop ^
 --Startup=auto --DisplayName=%SERVICE_DISPLAY_NAME% --Description=%SERVICE_DESCRIPTION% ^
 ++StartParams=%BASE_DIR% ++StartParams=%LOG_DIR% --LogPath=%LOG_DIR%



echo Installation of %SERVICE_NAME% is complete
sc start %SERVICE_NAME% 
rem %PRUNSRV% //RS//%SERVICE_NAME% 
echo Finished
pause