package cn.oyzh.easyshell.redis.key;

/**
 * @author oyzh
 * @since 2024-12-02
 */
public interface ShellRedisKeyValue<V> {

    V getValue();

    boolean hasValue();

    void setValue(V value);

    Object getUnSavedValue();

    void clearUnSavedValue();

    boolean hasUnSavedValue();

    void setUnSavedValue(Object unSavedValue);
}
