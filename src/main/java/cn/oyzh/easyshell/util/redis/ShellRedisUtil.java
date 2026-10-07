package cn.oyzh.easyshell.util.redis;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.redis.key.ShellRedisKey;

import java.util.Collection;

/**
 *
 * @author oyzh
 * @since 2026-10-07
 */
public class ShellRedisUtil {

    /**
     * 是否被排除
     *
     * @param keyTypes 键类型
     * @param node     键
     * @return 结果
     */
    public static boolean isExclude(Collection<String> keyTypes, ShellRedisKey node) {
        if (CollectionUtil.isEmpty(keyTypes)) {
            return true;
        }
        if (!keyTypes.contains("list") && node.isListKey()) {
            return true;
        }
        if (!keyTypes.contains("set") && node.isSetKey()) {
            return true;
        }
        if (!keyTypes.contains("zset") && node.isZSetKey()) {
            return true;
        }
        if (!keyTypes.contains("hash") && node.isHashKey()) {
            return true;
        }
        if (!keyTypes.contains("stream") && node.isStreamKey()) {
            return true;
        }
        if (!keyTypes.contains("json") && node.isJsonKey()) {
            return true;
        }
        return !keyTypes.contains("string") && node.isStringKey();
    }
}
