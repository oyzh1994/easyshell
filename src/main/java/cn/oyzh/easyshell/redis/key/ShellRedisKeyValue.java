package cn.oyzh.easyshell.redis.key;

/**
 * redis键值
 *
 * @author oyzh
 * @since 2025-09-01
 */
public interface ShellRedisKeyValue<V> {

    /**
     * 获取值
     *
     * @return 值
     */
    V getValue();

    /**
     * 是否包含值
     *
     * @return 结果
     */
    boolean hasValue();

    /**
     * 设置值
     *
     * @param value 值
     */
    void setValue(V value);

    /**
     * 获取未保存的值
     *
     * @return 未保存的值
     */
    Object getUnSavedValue();

    /**
     * 清除未保存的值
     */
    void clearUnSavedValue();

    /**
     * 是否包含未保存的值
     *
     * @return 结果
     */
    boolean hasUnSavedValue();

    /**
     * 设置未保存的值
     *
     * @param unSavedValue 未保存的值
     */
    void setUnSavedValue(Object unSavedValue);
}
