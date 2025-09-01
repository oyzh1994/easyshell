package cn.oyzh.easyshell.dto.redis;

import cn.oyzh.common.util.StringUtil;

import java.util.stream.Stream;

/**
 * @author oyzh
 * @since 2023/7/05
 */
public class RedisServerInfo {

    private String version;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public static RedisServerInfo parse(String str) {
        RedisServerInfo serverInfo = new RedisServerInfo();
        if (StringUtil.isNotBlank(str)) {
            Stream<String> lines = str.lines();
            lines.forEach(l -> {
                if (l.startsWith("redis_version:")) {
                    serverInfo.version = l.replace("redis_version:", "");
                }
            });
        }
        return serverInfo;
    }
}
