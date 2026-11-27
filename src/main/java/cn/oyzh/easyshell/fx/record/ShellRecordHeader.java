package cn.oyzh.easyshell.fx.record;

/**
 * 记录表格列头。
 * <p>
 * 用于统一处理「显示字段类型」「显示注释」开关后的列头刷新，
 * 让所有协议的记录表格（表、视图、查询结果）保持一致的列头外观。
 *
 * @author oyzh
 * @since 2026-11-27
 */
public interface ShellRecordHeader {

    /**
     * 刷新列头。
     */
    void refreshHeader();
}
