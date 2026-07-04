package org.mosh4j.core;

import org.mosh4j.crypto.MoshKey;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Entry point to run mosh4j as a server for testing.
 * Prints "MOSH CONNECT &lt;port&gt; &lt;key&gt;" so a client can connect.
 * <p>
 * Usage: port and key can be set via env MOSH_PORT, MOSH_KEY, or default port 60001 and a random key.
 * The session key is redacted unless MOSH_PRINT_KEY=1 (or true) is set, to avoid leaking secrets in logs.
 */
public final class MoshServerMain {

    private static final int DEFAULT_PORT = 60001;
    private static final int WIDTH = 80;
    private static final int HEIGHT = 24;

    public static void main(String[] args) throws Exception {
        int port = DEFAULT_PORT;
        String portEnv = System.getenv("MOSH_PORT");
        if (portEnv != null && !portEnv.isEmpty()) {
            try {
                port = Integer.parseInt(portEnv.trim());
            } catch (NumberFormatException e) {
                System.err.println("Invalid MOSH_PORT, using default " + DEFAULT_PORT + ": " + portEnv);
                port = DEFAULT_PORT;
            }
        }
        if (args.length > 0 && args[0] != null && !args[0].isEmpty()) {
            try {
                port = Integer.parseInt(args[0].trim());
            } catch (NumberFormatException e) {
                System.err.println("Invalid port argument, using " + port + ": " + args[0]);
            }
        }
        if (port <= 0 || port > 65535) {
            System.err.println("Port out of range 1-65535, using " + DEFAULT_PORT);
            port = DEFAULT_PORT;
        }

        String keyBase64 = System.getenv("MOSH_KEY");
        if (keyBase64 == null || keyBase64.trim().length() != 22) {
            keyBase64 = generateKeyBase64();
        } else {
            keyBase64 = keyBase64.trim();
        }

        MoshKey key = MoshKey.fromBase64(keyBase64);
        MoshServerSession session = new MoshServerSession(port, key, WIDTH, HEIGHT);

        boolean printKey = "1".equals(System.getenv("MOSH_PRINT_KEY")) || "true".equalsIgnoreCase(System.getenv("MOSH_PRINT_KEY"));
        if (printKey) {
            System.err.println("MOSH CONNECT " + port + " " + keyBase64);
        } else {
            System.err.println("MOSH CONNECT " + port + " <key redacted; set MOSH_PRINT_KEY=1 to show>");
        }
        System.err.flush();

        boolean firstContact = true;
        while (session.isRunning()) {
            boolean got = session.receiveOnce();
            if (!got) break;
            if (firstContact) {
                firstContact = false;
                byte[] banner = ("mosh4j test server (port " + port + ")\r\n").getBytes(StandardCharsets.UTF_8);
                session.feedHostOutput(banner);
            }
        }
        session.close();
    }

    private static String generateKeyBase64() {
        byte[] key = new byte[16];
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().withoutPadding().encodeToString(key).substring(0, 22);
    }
}
