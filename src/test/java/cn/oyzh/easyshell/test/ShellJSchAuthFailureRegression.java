package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import com.jcraft.jsch.JSchException;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 验证错误账户或密码会快速触发交互认证，而不是等待 JSch 重复尝试到服务端断开。
 */
public final class ShellJSchAuthFailureRegression {

    private ShellJSchAuthFailureRegression() {
    }

    public static void main(String[] args) {
        try {
            run();
            System.out.println("JSch auth failure regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        int hostIndex = Integer.getInteger("test.auth.host.index", 1);
        long maxFailureMillis = Long.getLong("test.auth.max.failure.millis", 10000L);
        ShellJSchTestHosts.Host host = ShellJSchTestHosts.load(hostIndex);
        System.out.println("Auth failure target: " + host);

        requireExceptionClassification(host);
        requireWrongPasswordCancellation(host, maxFailureMillis);
        requireRetryAfterWrongPassword(host);
        requireRetryAfterWrongUser(host);
    }

    private static void requireExceptionClassification(ShellJSchTestHosts.Host host) throws Exception {
        ShellSSHClient client = new ShellSSHClient(host.newConnect());
        ShellJSchTestHosts.acceptHostKeys(client);
        try {
            Method method = ShellSSHClient.class.getSuperclass()
                    .getDeclaredMethod("isAuthenticationFailure", JSchException.class);
            method.setAccessible(true);
            require((Boolean) method.invoke(client, new JSchException(
                    "SSH_MSG_DISCONNECT: 2 Too many authentication failures")),
                    "too-many-auth exception was not classified as authentication failure");
            require((Boolean) method.invoke(client, new JSchException("Auth cancel for methods 'publickey,password'")),
                    "auth-cancel exception was not classified as authentication failure");
            require(!(Boolean) method.invoke(client, new JSchException("Connection refused")),
                    "network exception was misclassified as authentication failure");
        } finally {
            client.close();
        }
        System.out.println("auth exception classification verified");
    }

    private static void requireWrongPasswordCancellation(ShellJSchTestHosts.Host host, long maxFailureMillis)
            throws Exception {
        ShellConnect connect = host.newConnect();
        connect.setPassword("invalid-" + UUID.randomUUID());
        AtomicInteger callbacks = new AtomicInteger();
        ShellSSHClient client = new ShellSSHClient(connect);
        ShellJSchTestHosts.acceptHostKeys(client);
        long start = System.currentTimeMillis();
        try {
            client.setVerifyFailureCallback(current -> {
                callbacks.incrementAndGet();
                return null;
            });
            try {
                client.start(15000);
                throw new IllegalStateException("wrong password unexpectedly authenticated");
            } catch (ShellException expected) {
                long elapsed = System.currentTimeMillis() - start;
                require(callbacks.get() == 1, "auth callback count mismatch: " + callbacks.get());
                require(elapsed < maxFailureMillis,
                        "auth failure callback was delayed: " + elapsed + "ms");
                System.out.println("wrong password cancellation: callbackCalls=" + callbacks.get()
                        + ", elapsed=" + elapsed + "ms, failure=" + expected.getClass().getSimpleName());
            }
        } finally {
            client.close();
        }
    }

    private static void requireRetryAfterWrongPassword(ShellJSchTestHosts.Host host) throws Exception {
        ShellConnect connect = host.newConnect();
        connect.setPassword("invalid-" + UUID.randomUUID());
        AtomicInteger callbacks = new AtomicInteger();
        try (ShellSSHClient client = new ShellSSHClient(connect)) {
            ShellJSchTestHosts.acceptHostKeys(client);
            client.setVerifyFailureCallback(current -> {
                callbacks.incrementAndGet();
                return host.newConnect();
            });
            client.start(15000);
            require(client.isConnected(), "corrected password did not authenticate");
            require(callbacks.get() == 1, "corrected password callback count mismatch: " + callbacks.get());
            require("auth-retry".equals(client.exec("echo auth-retry", 10000)),
                    "corrected password command failed");
        }
        System.out.println("wrong password retry: callbackCalls=" + callbacks.get() + ", connected=true");
    }

    private static void requireRetryAfterWrongUser(ShellJSchTestHosts.Host host) throws Exception {
        ShellConnect connect = host.newConnect();
        connect.setUser("missing_" + UUID.randomUUID().toString().replace("-", ""));
        AtomicInteger callbacks = new AtomicInteger();
        try (ShellSSHClient client = new ShellSSHClient(connect)) {
            ShellJSchTestHosts.acceptHostKeys(client);
            client.setVerifyFailureCallback(current -> {
                callbacks.incrementAndGet();
                return host.newConnect();
            });
            client.start(15000);
            require(client.isConnected(), "corrected user did not authenticate");
            require(callbacks.get() == 1, "corrected user callback count mismatch: " + callbacks.get());
            require("auth-retry".equals(client.exec("echo auth-retry", 10000)),
                    "corrected user command failed");
        }
        System.out.println("wrong user retry: callbackCalls=" + callbacks.get() + ", connected=true");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
