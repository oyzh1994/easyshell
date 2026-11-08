package cn.oyzh.easyshell.http;

import cn.oyzh.common.util.StringUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;

/**
 * HTTP 键值行，用于请求参数、请求头、表单和环境变量。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpKeyValue implements Serializable {

    /**
     * 是否启用
     */
    private final BooleanProperty enabled = new SimpleBooleanProperty(true);

    /**
     * 键
     */
    private final StringProperty key = new SimpleStringProperty("");

    /**
     * 值
     */
    private final StringProperty value = new SimpleStringProperty("");

    /**
     * 是否文件
     */
    private final BooleanProperty file = new SimpleBooleanProperty(false);

    /**
     * 创建空行
     */
    public ShellHttpKeyValue() {
    }

    /**
     * 创建键值行
     *
     * @param key   键
     * @param value 值
     */
    public ShellHttpKeyValue(String key, String value) {
        this.key.set(key == null ? "" : key);
        this.value.set(value == null ? "" : value);
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    public BooleanProperty enabledProperty() {
        return enabled;
    }

    public String getKey() {
        return key.get();
    }

    public void setKey(String key) {
        this.key.set(key == null ? "" : key);
    }

    public StringProperty keyProperty() {
        return key;
    }

    public String getValue() {
        return value.get();
    }

    public void setValue(String value) {
        this.value.set(value == null ? "" : value);
    }

    public StringProperty valueProperty() {
        return value;
    }

    public boolean isFile() {
        return file.get();
    }

    public void setFile(boolean file) {
        this.file.set(file);
    }

    public BooleanProperty fileProperty() {
        return file;
    }

    /**
     * 是否为空行。
     *
     * @return 结果
     */
    public boolean isEmpty() {
        return StringUtil.isBlank(this.getKey()) && StringUtil.isBlank(this.getValue());
    }

    /**
     * 复制当前行。
     *
     * @return 新行
     */
    public ShellHttpKeyValue copy() {
        ShellHttpKeyValue copy = new ShellHttpKeyValue(this.getKey(), this.getValue());
        copy.setEnabled(this.isEnabled());
        copy.setFile(this.isFile());
        return copy;
    }

    @Override
    public String toString() {
        return this.getKey() + "=" + this.getValue();
    }
}
