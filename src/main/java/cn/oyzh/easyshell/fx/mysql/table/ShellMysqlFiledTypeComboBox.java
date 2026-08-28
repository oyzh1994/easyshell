package cn.oyzh.easyshell.fx.mysql.table;

import cn.oyzh.easyshell.data.db.DBDialect;
import cn.oyzh.easyshell.db.DBColumnFieldManager;
import cn.oyzh.easyshell.util.mysql.ShellMysqlColumnUtil;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

import java.util.List;

/**
 * db字段类型选择框
 *
 * @author oyzh
 * @since 2024/07/03
 */
public class ShellMysqlFiledTypeComboBox extends FXComboBox<String> {

    /**
     * 是否支持长度
     *
     * @return 结果
     */
    public boolean supportSize() {
        return DBColumnFieldManager.supportSize(DBDialect.MYSQL, this.getSelectedItem());
    }

    /**
     * 是否支持字符集及排序
     *
     * @return 结果
     */
    public boolean supportCharset() {
        return DBColumnFieldManager.supportCharset(DBDialect.MYSQL, this.getSelectedItem());
    }

    /**
     * 是否支持无符号
     *
     * @return 结果
     */
    public boolean supportUnsigned() {
        return DBColumnFieldManager.supportUnsigned(DBDialect.MYSQL, this.getSelectedItem());
    }

    /**
     * 是否支持小数
     *
     * @return 结果
     */
    public boolean supportDigits() {
        return DBColumnFieldManager.supportDigits(DBDialect.MYSQL, this.getSelectedItem());
    }

    /**
     * 是否支持自动递增
     *
     * @return 结果
     */
    public boolean supportAutoIncrement() {
        return DBColumnFieldManager.supportAutoIncrement(DBDialect.MYSQL, this.getSelectedItem());
    }

    /**
     * 是否支持默认值
     *
     * @return 结果
     */
    public boolean supportDefaultValue() {
        return DBColumnFieldManager.supportDefaultValue(DBDialect.MYSQL, this.getSelectedItem());
    }

    /**
     * 是否支持当前时间戳
     *
     * @return 结果
     */
    public boolean supportTimestamp() {
        return DBColumnFieldManager.supportTimestamp(DBDialect.MYSQL, this.getSelectedItem());
    }

    /**
     * 是否支持集合
     *
     * @return 结果
     */
    public boolean supportGeometry() {
        return DBColumnFieldManager.supportGeometry(DBDialect.MYSQL, this.getSelectedItem());
    }

    /**
     * 是否支持json
     *
     * @return 结果
     */
    public boolean supportJson() {
        return DBColumnFieldManager.supportJson(DBDialect.MYSQL, this.getSelectedItem());
    }

    /**
     * 是否支持主键
     *
     * @return 结果
     */
    public boolean supportEnum() {
        return DBColumnFieldManager.supportEnum(DBDialect.MYSQL, this.getSelectedItem());
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
        return DBColumnFieldManager.exampleValue(DBDialect.MYSQL, this.getSelectedItem());
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

    @Override
    public void initNode() {
        List<String> list = DBColumnFieldManager.fieldNames(DBDialect.MYSQL);
        this.setItem(list);
        super.initNode();
    }
}
