package cn.oyzh.easyshell.redis.key;

/**
 * @author oyzh
 * @since 2024-12-02
 */
public interface ShellRedisKeyRow extends Cloneable {

    String getValue();

    void setValue(String value);
}
