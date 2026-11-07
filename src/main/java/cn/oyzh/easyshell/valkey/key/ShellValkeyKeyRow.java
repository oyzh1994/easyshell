package cn.oyzh.easyshell.valkey.key;

/**
 * redis键行
 *
 * @author oyzh
 * @since 2025-09-01
 */
public interface ShellValkeyKeyRow extends Cloneable {

    /**
     * 获取值
     *
     * @return 值
     */
    String getValue();

    /**
     * 设置值
     *
     * @param value 值
     */
    void setValue(String value);
}
