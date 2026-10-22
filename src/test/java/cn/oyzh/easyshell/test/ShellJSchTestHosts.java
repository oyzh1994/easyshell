package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellJumpConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 从本地主机清单读取集成测试配置。凭据只保留在运行内存中，不输出到日志。
 */
public final class ShellJSchTestHosts {

    private static final String HOSTS_FILE_PROPERTY = "test.hosts.file";

    private ShellJSchTestHosts() {
    }

    public static Host load(int index) throws IOException {
        String file = System.getProperty(HOSTS_FILE_PROPERTY);
        if (file == null || file.isBlank()) {
            throw new IllegalArgumentException(HOSTS_FILE_PROPERTY + " is required");
        }
        return parse(Path.of(file)).stream()
                .filter(host -> host.index() == index)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("test host index not found: " + index));
    }

    private static List<Host> parse(Path file) throws IOException {
        List<Host> hosts = new ArrayList<>();
        int index = 0;
        String label = null;
        Map<String, String> values = new LinkedHashMap<>();
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if (trimmed.matches("^\\d+\\.\\s*.*")) {
                if (index > 0) {
                    hosts.add(toHost(index, label, values));
                }
                int dot = trimmed.indexOf('.');
                index = Integer.parseInt(trimmed.substring(0, dot));
                label = trimmed.substring(dot + 1).trim();
                if (label.endsWith(",")) {
                    label = label.substring(0, label.length() - 1).trim();
                }
                values = new LinkedHashMap<>();
                continue;
            }
            int separator = Math.max(trimmed.indexOf(':'), trimmed.indexOf('：'));
            if (separator > 0) {
                values.put(trimmed.substring(0, separator).trim(), trimmed.substring(separator + 1).trim());
            }
        }
        if (index > 0) {
            hosts.add(toHost(index, label, values));
        }
        return hosts;
    }

    private static Host toHost(int index, String label, Map<String, String> values) {
        String host = values.get("主机");
        String user = values.get("用户名");
        String password = values.get("密码");
        String port = values.getOrDefault("端口", "22");
        if (host == null || user == null || password == null) {
            throw new IllegalArgumentException("test host entry is incomplete: " + index);
        }
        return new Host(index, label, host, Integer.parseInt(port), user, password);
    }

    public record Host(int index, String label, String host, int port, String user, String password) {

        public ShellConnect newConnect() {
            ShellConnect connect = new ShellConnect();
            connect.setHost(this.host + ":" + this.port);
            connect.setUser(this.user);
            connect.setPassword(this.password);
            connect.setAuthMethod("password");
            connect.setCharset("UTF-8");
            connect.setConnectTimeOut(15000);
            return connect;
        }

        public ShellJumpConfig newJumpConfig() {
            ShellJumpConfig config = new ShellJumpConfig();
            config.setHost(this.host);
            config.setPort(this.port);
            config.setUser(this.user);
            config.setPassword(this.password);
            config.setAuthMethod("password");
            config.setTimeout(15000);
            return config;
        }

        @Override
        public String toString() {
            return "Host[index=" + this.index + ", label=" + this.label + "]";
        }
    }
}
