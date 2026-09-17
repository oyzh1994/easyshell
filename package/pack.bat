@echo off
chcp 65001 >nul
set ARG1=%1
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8
call mvn package -DskipTests -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -Dproject.build.sourceEncoding=UTF-8
rem call mvn test-compile
call mvn exec:java -Dexec.mainClass="cn.oyzh.easyshell.test.Pack" -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -Dproject.build.sourceEncoding=UTF-8 -Dexec.jvmArgs="-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8" -Dexec.args="%ARG1%" -Dexec.testClasspathScope=test -X
