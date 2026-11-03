package cn.oyzh.easyshell.fx.sqlite.table;

import cn.oyzh.easyshell.popups.sqlite.ShellSqliteIndexFieldPopupController;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.fx.gui.text.field.ChooseTextField;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;
import cn.oyzh.i18n.I18nHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * SQLite索引字段选择框。
 *
 * @author oyzh
 * @since 2026-11-03
 */
public class ShellSqliteIndexFieldTextFiled extends ChooseTextField {

    private List<? extends SQLiteColumn> columnList;

    private Set<String> columns;

    private PopupAdapter popup;

    public ShellSqliteIndexFieldTextFiled() {
    }

    /**
     * 构造索引字段选择框。
     *
     * @param columnList 可选字段列表
     * @param columns    已选字段列表
     */
    public ShellSqliteIndexFieldTextFiled(List<? extends SQLiteColumn> columnList, Set<String> columns) {
        this.columnList = columnList;
        this.setColumns(columns);
    }

    /**
     * 设置可选字段列表。
     *
     * @param columnList 可选字段列表
     */
    public void setColumnList(List<? extends SQLiteColumn> columnList) {
        this.columnList = columnList;
    }

    protected void initPopup() {
        this.disable();
        this.popup = PopupManager.parsePopup(ShellSqliteIndexFieldPopupController.class);
        this.popup.setProp("columns", this.columns);
        this.popup.setProp("columnList", this.columnList);
        this.popup.setProp("onSubmit", (Runnable) () -> {
            this.enable();
            this.skin().resetButtonColor();
            ShellSqliteIndexColumnListView listView = this.listView();
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
     * 设置已选字段列表。
     *
     * @param columns 字段列表
     */
    public void setColumns(Set<String> columns) {
        this.columns = columns == null ? new HashSet<>() : columns;
        this.initText();
    }

    protected void initText() {
        String text = String.join(", ", this.columns);
        this.setText(text);
        this.setTipText(text);
    }

    protected ShellSqliteIndexColumnListView listView() {
        if (this.popup != null && this.popup.content() != null) {
            return (ShellSqliteIndexColumnListView) this.popup.content().lookup("#listView");
        }
        return null;
    }

    public Set<String> getColumns() {
        return this.columns;
    }

    @Override
    public void initNode() {
        super.setAction(this::initPopup);
        this.setPromptText(I18nHelper.pleaseSelectField());
        super.initNode();
    }
}
