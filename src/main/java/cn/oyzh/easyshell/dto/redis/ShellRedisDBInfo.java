package cn.oyzh.easyshell.dto.redis;

import cn.oyzh.common.util.StringUtil;

/**
 * redis数据库信息
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellRedisDBInfo {

    /** 键数量 */
    private int keys;

    /** 获取键数量 */
    public int getKeys() {
        return keys;
    }

    /** 设置键数量 */
    public void setKeys(int keys) {
        this.keys = keys;
    }

    /** 获取db索引 */
    public int getIndex() {
        return index;
    }

    /** 设置db索引 */
    public void setIndex(int index) {
        this.index = index;
    }

    /** 获取过期键数量 */
    public int getExpires() {
        return expires;
    }

    /** 设置过期键数量 */
    public void setExpires(int expires) {
        this.expires = expires;
    }

    /** 获取平均存活时间 */
    public double getAvgTTL() {
        return avgTTL;
    }

    /** 设置平均存活时间 */
    public void setAvgTTL(double avgTTL) {
        this.avgTTL = avgTTL;
    }

    /** db索引 */
    private int index;

    /** 过期键数量 */
    private int expires;

    /** 平均存活时间 */
    private double avgTTL;

    /**
     * 解析redis数据库信息字符串
     *
     * @param str 数据库信息字符串
     * @return redis数据库信息
     */
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
