@echo off
chcp 65001
set ARG1=%1
call mvn package -DskipTests
rem call mvn test-compile
call mvn exec:java -Dexec.mainClass="cn.oyzh.easyshell.test.Pack" -Dexec.args="%ARG1%" -Dexec.testClasspathScope=test -X
