package cn.oyzh.easyshell.dto.redis;

import cn.oyzh.common.util.StringUtil;

/**
 * @author oyzh
 * @since 2023/6/30
 */
public class ShellRedisDBInfo {

    private int keys;

    public int getKeys() {
        return keys;
    }

    public void setKeys(int keys) {
        this.keys = keys;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getExpires() {
        return expires;
    }

    public void setExpires(int expires) {
        this.expires = expires;
    }

    public double getAvgTTL() {
        return avgTTL;
    }

    public void setAvgTTL(double avgTTL) {
        this.avgTTL = avgTTL;
    }

    private int index;

    private int expires;

    private double avgTTL;

    public static ShellRedisDBInfo parse(String str) {
        ShellRedisDBInfo dbInfo = new ShellRedisDBInfo();
        if (StringUtil.isNotBlank(str)) {
            str = str.substring(2);
            String indexStr = str.substring(0, str.indexOf(":"));
            dbInfo.index = Integer.parseInt(indexStr);
            str = str.substring(str.indexOf(":") + 1);
            String[] strs = str.split(",");
            for (String s : strs) {
                if (s.startsWith("keys=")) {
                    dbInfo.keys = Integer.parseInt(s.replace("keys=", ""));
                } else if (s.startsWith("expires=")) {
                    dbInfo.expires = Integer.parseInt(s.replace("expires=", ""));
                } else if (s.startsWith("avg_ttl=")) {
                    dbInfo.avgTTL = Integer.parseInt(s.replace("avg_ttl=", ""));
                }
            }
        }
        return dbInfo;
    }
}
