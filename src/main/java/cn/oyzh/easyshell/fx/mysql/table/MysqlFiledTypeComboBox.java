package cn.oyzh.easyshell.fx.mysql.table;

import cn.oyzh.easyshell.util.mysql.ShellMysqlColumnUtil;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

/**
 * db字段类型选择框
 *
 * @author oyzh
 * @since 2024/07/03
 */
public class MysqlFiledTypeComboBox extends FXComboBox<String> {

    {
        this.setItem(ShellMysqlColumnUtil.fields());
    }

    /**
     * 是否支持长度
     *
     * @return 结果
     */
    public boolean supportSize() {
        return ShellMysqlColumnUtil.supportSize(this.getSelectedItem());
    }

    /**
     * 是否支持字符集及排序
     *
     * @return 结果
     */
    public boolean supportCharset() {
        return ShellMysqlColumnUtil.supportCharset(this.getSelectedItem());
    }

    /**
     * 是否支持无符号
     *
     * @return 结果
     */
    public boolean supportUnsigned() {
        return ShellMysqlColumnUtil.supportUnsigned(this.getSelectedItem());
    }

    /**
     * 是否支持小数
     *
     * @return 结果
     */
    public boolean supportDigits() {
        return ShellMysqlColumnUtil.supportDigits(this.getSelectedItem());
    }

    /**
     * 是否支持自动递增
     *
     * @return 结果
     */
    public boolean supportAutoIncrement() {
        return ShellMysqlColumnUtil.supportAutoIncrement(this.getSelectedItem());
    }

    /**
     * 是否支持默认值
     *
     * @return 结果
     */
    public boolean supportDefaultValue() {
        return ShellMysqlColumnUtil.supportDefaultValue(this.getSelectedItem());
    }

    /**
     * 是否支持当前时间戳
     *
     * @return 结果
     */
    public boolean supportTimestamp() {
        return ShellMysqlColumnUtil.supportTimestamp(this.getSelectedItem());
    }

    /**
     * 是否支持集合
     *
     * @return 结果
     */
    public boolean supportGeometry() {
        return ShellMysqlColumnUtil.supportGeometry(this.getSelectedItem());
    }

    /**
     * 是否支持json
     *
     * @return 结果
     */
    public boolean supportJson() {
        return ShellMysqlColumnUtil.supportJson(this.getSelectedItem());
    }

    /**
     * 是否支持主键
     *
     * @return 结果
     */
    public boolean supportEnum() {
        return ShellMysqlColumnUtil.supportEnum(this.getSelectedItem());
    }

    /**
     * 是否支持值
     *
     * @return 结果
     */
    public boolean supportValue() {
        return false;
    }

    /**
     * 获取示例值
     *
     * @return 示例值
     */
    public Object exampleValue() {
        return ShellMysqlColumnUtil.exampleValue(this.getSelectedItem());
    }

    /**
     * 获取字段默认值
     *
     * @return 默认值
     */
    public Object defaultValue() {
        return ShellMysqlColumnUtil.defaultValue(this.getSelectedItem());
    }

    @Override
    public void select(String type) {
        if (type != null) {
            super.select(type.toUpperCase());
        }
    }
}
