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

rem Batch script for removing the ProcrunService (JVM and Java versions)

setlocal

rem The service names (make sure they does not clash with an existing service)
set SERVICE_JVM=${artifactId}

rem my location
set BIN_PATH=%~dp0

rem location of Prunsrv
set PATH_PRUNSRV=%BIN_PATH%
set BASE_DIR=%PATH_PRUNSRV%\..
set LOG_DIR=%BASE_DIR%\logs

rem location of Prunsrv
rem Allow prunsrv to be overridden
if "%PRUNSRV%" == "" set PRUNSRV=%PATH_PRUNSRV%prunsrv

echo Removing %SERVICE_JVM%
%PRUNSRV% //DS//%SERVICE_JVM% --LogPath=%LOG_DIR%


echo Finished
pause
