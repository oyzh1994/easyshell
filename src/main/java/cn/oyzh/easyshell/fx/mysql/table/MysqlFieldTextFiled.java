package cn.oyzh.easyshell.fx.mysql.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.popups.mysql.ShellMysqlColumnFieldPopupController;
import cn.oyzh.fx.gui.text.field.ChooseTextField;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;
import cn.oyzh.i18n.I18nHelper;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author oyzh
 * @since 2024/7/10
 */
public class MysqlFieldTextFiled extends ChooseTextField {

    {
        super.setAction(this::initPopup);
        this.setPromptText(I18nHelper.pleaseSelectField());
    }

    public MysqlFieldTextFiled() {
    }

    private List<MysqlColumn> columns;

    private List<String> selectedColumns;

    public MysqlFieldTextFiled(List<MysqlColumn> columns, List<String> selectedColumns) {
        this.columns = columns;
        this.setSelectedColumns(selectedColumns);
    }

    private PopupAdapter popup;

    protected void initPopup() {
        this.popup = PopupManager.parsePopup(ShellMysqlColumnFieldPopupController.class);
        this.popup.setProp("columns", this.columns);
        this.popup.setProp("selectedColumns", this.selectedColumns);
        this.popup.setProp("onSubmit", (Runnable) () -> {
            MysqlColumnListView listView = this.listView();
            if (listView != null) {
                this.selectedColumns = listView.getSelectedColumnNames();
            }
            this.initText();
        });
        this.popup.showPopup(this);
    }

    public void setColumns(List<MysqlColumn> columns) {
        this.columns = columns;
        MysqlColumnListView listView = this.listView();
        if (listView != null) {
            listView.init(columns);
        }
        this.initText();
    }

    public void setSelectedColumns(List<String> selectedColumns) {
        this.selectedColumns = selectedColumns;
        MysqlColumnListView listView = this.listView();
        if (listView != null) {
            listView.select(selectedColumns);
        }
        this.initText();
    }

    public List<String> getSelectedColumns() {
        return Objects.requireNonNullElse(this.selectedColumns, Collections.emptyList());
    }

    protected void initText() {
        String text = "";
        if (CollectionUtil.isNotEmpty(this.selectedColumns)) {
            text = CollectionUtil.join(this.selectedColumns, ",");
        }
        this.setText(text);
        this.setTipText(text);
    }

    protected MysqlColumnListView listView() {
        if (this.popup != null && this.popup.content() != null) {
            return (MysqlColumnListView) this.popup.content().lookup("#listView");
        }
        return null;
    }
}
