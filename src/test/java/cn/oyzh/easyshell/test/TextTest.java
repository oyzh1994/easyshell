package cn.oyzh.easyshell.test;

import org.junit.Test;

public class TextTest {


    @Test
    public void test() {

        String str = """
                java.base@23.0.1
                java.compiler@23.0.1
                java.datatransfer@23.0.1
                java.desktop@23.0.1
                java.instrument@23.0.1
                java.logging@23.0.1
                java.management@23.0.1
                java.management.rmi@23.0.1
                java.naming@23.0.1
                java.net.http@23.0.1
                java.prefs@23.0.1
                java.rmi@23.0.1
                java.scripting@23.0.1
                java.se@23.0.1
                java.security.jgss@23.0.1
                java.security.sasl@23.0.1
                java.smartcardio@23.0.1
                java.sql@23.0.1
                java.sql.rowset@23.0.1
                java.transaction.xa@23.0.1
                java.xml@23.0.1
                java.xml.crypto@23.0.1
                jdk.accessibility@23.0.1
                jdk.attach@23.0.1
                jdk.charsets@23.0.1
                jdk.compiler@23.0.1
                jdk.crypto.cryptoki@23.0.1
                jdk.crypto.ec@23.0.1
                jdk.crypto.mscapi@23.0.1
                jdk.dynalink@23.0.1
                jdk.editpad@23.0.1
                jdk.graal.compiler@23.0.1
                jdk.graal.compiler.management@23.0.1
                jdk.hotspot.agent@23.0.1
                jdk.httpserver@23.0.1
                jdk.incubator.vector@23.0.1
                jdk.internal.ed@23.0.1
                jdk.internal.jvmstat@23.0.1
                jdk.internal.le@23.0.1
                jdk.internal.md@23.0.1
                jdk.internal.opt@23.0.1
                jdk.internal.vm.ci@23.0.1
                jdk.jartool@23.0.1
                jdk.javadoc@23.0.1
                jdk.jcmd@23.0.1
                jdk.jconsole@23.0.1
                jdk.jdeps@23.0.1
                jdk.jdi@23.0.1
                jdk.jdwp.agent@23.0.1
                jdk.jfr@23.0.1
                jdk.jlink@23.0.1
                jdk.jpackage@23.0.1
                jdk.jshell@23.0.1
                jdk.jsobject@23.0.1
                jdk.jstatd@23.0.1
                jdk.localedata@23.0.1
                jdk.management@23.0.1
                jdk.management.agent@23.0.1
                jdk.management.jfr@23.0.1
                jdk.naming.dns@23.0.1
                jdk.naming.dns@23.0.1
                jdk.naming.rmi@23.0.1
                jdk.net@23.0.1
                jdk.nio.mapmode@23.0.1
                jdk.sctp@23.0.1
                jdk.security.auth@23.0.1
                jdk.security.jgss@23.0.1
                jdk.unsupported@23.0.1
                jdk.unsupported.desktop@23.0.1
                jdk.xml.dom@23.0.1
                jdk.zipfs@23.0.1
                """;
        StringBuilder sb = new StringBuilder();
        for (String s : str.split("\n")) {
            if (s.contains("@")) {
                s = s.split("@")[0];
            }
            sb.append("\"").append(s).append("\"").append(",\n");
        }
        System.out.println(sb.toString());
    }
}
