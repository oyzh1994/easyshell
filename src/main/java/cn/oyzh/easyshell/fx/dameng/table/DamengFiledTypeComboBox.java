package cn.oyzh.easyshell.fx.dameng.table;//package cn.oyzh.easyshell.fx.dameng.table;
//
//import cn.oyzh.easyshell.util.dameng.DamengColumnUtil;
//import cn.oyzh.fx.db.DBDialect;
//import cn.oyzh.fx.db.DBColumnFieldManager;
//import cn.oyzh.fx.plus.controls.combo.FXComboBox;
//
///**
// * db字段类型选择框
// *
// * @author oyzh
// * @since 2024/07/03
// */
//public class DamengFiledTypeComboBox extends FXComboBox<String> {
//
//    {
//        this.setItem(DamengColumnUtil.fields());
//    }
//
//    /**
//     * 是否支持长度
//     *
//     * @return 结果
//     */
//    public boolean supportSize() {
//        return DBColumnFieldManager.supportSize(DBDialect.DAMENG, this.getSelectedItem());
//    }
//
//    /**
//     * 是否支持小数
//     *
//     * @return 结果
//     */
//    public boolean supportDigits() {
//        return DBColumnFieldManager.supportDigits(DBDialect.DAMENG, this.getSelectedItem());
//    }
//
//    /**
//     * 是否支持自动递增
//     *
//     * @return 结果
//     */
//    public boolean supportAutoIncrement() {
//        return DBColumnFieldManager.supportAutoIncrement(DBDialect.DAMENG, this.getSelectedItem());
//    }
//
//    /**
//     * 是否支持默认值
//     *
//     * @return 结果
//     */
//    public boolean supportDefaultValue() {
//        return DBColumnFieldManager.supportDefaultValue(DBDialect.DAMENG, this.getSelectedItem());
//    }
//
//    /**
//     * 是否支持当前时间戳
//     *
//     * @return 结果
//     */
//    public boolean supportTimestamp() {
//        return DBColumnFieldManager.supportTimestamp(DBDialect.DAMENG, this.getSelectedItem());
//    }
//
//    /**
//     * 是否支持json
//     *
//     * @return 结果
//     */
//    public boolean supportJson() {
//        return DBColumnFieldManager.supportJson(DBDialect.DAMENG, this.getSelectedItem());
//    }
//
//    /**
//     * 是否支持值
//     *
//     * @return 结果
//     */
//    public boolean supportValue() {
//        return false;
//    }
//
//    /**
//     * 获取示例值
//     *
//     * @return 示例值
//     */
//    public Object exampleValue() {
//        return DBColumnFieldManager.exampleValue(DBDialect.DAMENG, this.getSelectedItem());
//    }
//
//    /**
//     * 获取字段默认值
//     *
//     * @return 默认值
//     */
//    public Object defaultValue() {
//        return DamengColumnUtil.defaultValue(this.getSelectedItem());
//    }
//
//    @Override
//    public void select(String type) {
//        if (type != null) {
//            super.select(type.toUpperCase());
//        }
//    }
//}
