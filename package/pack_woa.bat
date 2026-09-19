@echo off
chcp 65001 >nul
set ARG1=%1
call mvn package -Djavafx.platform=win -DskipTests
rem call mvn test-compile
call mvn exec:java -Djavafx.platform=win -Dexec.mainClass="cn.oyzh.easyshell.test.Pack" -Dexec.args="%ARG1%" -Dexec.testClasspathScope=test -X
