package cn.oyzh.easyshell.dto.redis;

import cn.oyzh.common.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * @author oyzh
 * @since 2023/6/30
 */
public class RedisRemoteInfo {

    private RedisServerInfo server;

    public RedisServerInfo getServer() {
        return server;
    }

    public void setServer(RedisServerInfo server) {
        this.server = server;
    }

    public List<RedisDBInfo> getKeyspace() {
        return keyspace;
    }

    public void setKeyspace(List<RedisDBInfo> keyspace) {
        this.keyspace = keyspace;
    }

    private List<RedisDBInfo> keyspace;

    public void addDBInfo(RedisDBInfo dbInfo) {
        if (dbInfo != null) {
            if (this.keyspace == null) {
                this.keyspace = new ArrayList<>(16);
            }
            this.keyspace.add(dbInfo);
        }
    }

    public int getDBCount() {
        if (CollectionUtil.isNotEmpty(this.keyspace)) {
            Optional<Integer> count = this.keyspace.parallelStream().map(RedisDBInfo::getIndex).max(Integer::compareTo);
            return count.map(i -> i + 1).orElse(-1);
        }
        return -1;
    }

    public String getServerVersion() {
        if (this.server == null) {
            return null;
        }
        return this.server.getVersion();
    }

    public boolean isServerVersionGE(String version) {
        if (version != null) {
            try {
                String serverVersion = this.getServerVersion();
                if (serverVersion != null) {
                    double s1 = Double.parseDouble(serverVersion.replace(".", ""));
                    double s2 = Double.parseDouble(version.replace(".", ""));
                    return s1 >= s2;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    public static RedisRemoteInfo parse(String info) {
        RedisRemoteInfo remoteInfo = new RedisRemoteInfo();
        if (info == null || info.isBlank()) {
            return remoteInfo;
        }
        Stream<String> lines = info.lines();
        AtomicReference<String> flag = new AtomicReference<>();
        lines.forEach(s -> {
            if (s.startsWith("#")) {
                if (s.startsWith("# Keyspace")) {
                    flag.set("Keyspace");
                } else if (s.startsWith("# Server")) {
                    flag.set("Server");
                } else {
                    flag.set(null);
                }
            } else if ("Keyspace".equals(flag.get())) {
                remoteInfo.addDBInfo(RedisDBInfo.parse(s));
            } else if ("Keyspace".equals(flag.get())) {
                remoteInfo.server = RedisServerInfo.parse(s);
            }
        });
        return remoteInfo;
    }


}
