package cn.oyzh.easyshell.fx.sqlserver.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumn;
import cn.oyzh.easyshell.sqlserver.index.SqlServerIndex;
import cn.oyzh.easyshell.popups.sqlserver.ShellSqlServerIndexFieldPopupController;
import cn.oyzh.fx.gui.text.field.ChooseTextField;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;
import cn.oyzh.i18n.I18nHelper;

import java.util.List;

/**
 * SqlServer索引字段选择框
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellSqlServerIndexFieldTextFiled extends ChooseTextField {

    {
        super.setAction(this::initPopup);
        this.setPromptText(I18nHelper.pleaseSelectField());
    }

    /**
     * 构造SqlServer索引字段选择框
     */
    public ShellSqlServerIndexFieldTextFiled() {
    }

    /**
     * 索引
     */
    private SqlServerIndex dbIndex;

    /**
     * 字段列表
     */
    private List<SqlServerColumn> columnList;

    /**
     * 已选中的索引字段列表
     */
    private List<SqlServerIndex.IndexColumn> columns;

    /**
     * 构造并初始化SqlServer索引字段选择框
     *
     * @param dbIndex    索引
     * @param columnList 字段列表
     * @param columns    已选中的索引字段列表
     */
    public ShellSqlServerIndexFieldTextFiled(SqlServerIndex dbIndex, List<SqlServerColumn> columnList, List<SqlServerIndex.IndexColumn> columns) {
        this.dbIndex = dbIndex;
        this.columnList = columnList;
        this.setColumns(columns);
    }

    /**
     * 弹出选择框
     */
    private PopupAdapter popup;

    /**
     * 初始化并弹出索引字段选择框
     */
    protected void initPopup() {
        this.disable();
        this.popup = PopupManager.parsePopup(ShellSqlServerIndexFieldPopupController.class);
        this.popup.setProp("dbIndex", this.dbIndex);
        this.popup.setProp("columns", this.columns);
        this.popup.setProp("columnList", this.columnList);
        this.popup.setProp("onSubmit", (Runnable) () -> {
            this.enable();
            this.skin().resetButtonColor();
            ShellSqlServerIndexColumnListView listView = this.listView();
            if (listView != null) {
                this.columns = listView.getColumns();
            }
            this.initText();
        });
        this.popup.popup().setOnHiding(event -> {
            this.enable();
            this.skin().resetButtonColor();
        });
        this.popup.showPopup(this);
    }

    /**
     * 设置已选中的索引字段
     *
     * @param columns 已选中的索引字段列表
     */
    public void setColumns(List<SqlServerIndex.IndexColumn> columns) {
        this.columns = columns;
        this.initText();
    }

    /**
     * 根据已选中的索引字段刷新文本显示
     */
    protected void initText() {
        String text;
        StringBuilder builder = new StringBuilder();
        if (CollectionUtil.isNotEmpty(this.columns)) {
            for (SqlServerIndex.IndexColumn column : this.columns) {
                builder.append(",");
                builder.append(column.getColumnName());
            }
            text = builder.substring(1);
        } else {
            text = "";
        }
        this.setText(text);
        this.setTipText(text);
    }

    /**
     * 获取弹出框内的索引字段选择列表
     *
     * @return 索引字段选择列表，弹出框未初始化时返回 null
     */
    protected ShellSqlServerIndexColumnListView listView() {
        if (this.popup != null && this.popup.content() != null) {
            return (ShellSqlServerIndexColumnListView) this.popup.content().lookup("#listView");
        }
        return null;
    }

    /**
     * 获取已选中的索引字段列表
     *
     * @return 已选中的索引字段列表
     */
    public List<SqlServerIndex.IndexColumn> getColumns() {
        return columns;
    }
}
