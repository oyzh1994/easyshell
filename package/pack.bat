@echo off
chcp 65001
set ARG1=%1
call mvn package -DskipTests -Dfile.encoding=UTF-8 -Dproject.build.sourceEncoding=UTF-8
rem call mvn test-compile
call mvn exec:java -Dexec.mainClass="cn.oyzh.easyshell.test.Pack" -Dfile.encoding=UTF-8 -Dproject.build.sourceEncoding=UTF-8 -Dexec.jvmArgs="-Dfile.encoding=UTF-8" -Dexec.args="%ARG1%" -Dexec.testClasspathScope=test -X
