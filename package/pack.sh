#!/bin/bash
ARG1=$1
mvn package -DskipTests
#mvn test-compile
mvn exec:java -Dexec.mainClass="cn.oyzh.easyshell.test.Pack" -Dexec.args="$ARG1" -Dexec.testClasspathScope=test -X