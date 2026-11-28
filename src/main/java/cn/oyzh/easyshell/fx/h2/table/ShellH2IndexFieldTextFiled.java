package cn.oyzh.easyshell.fx.h2.table;

import cn.oyzh.easyshell.popups.h2.ShellH2IndexFieldPopupController;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.fx.gui.text.field.ChooseTextField;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;
import cn.oyzh.i18n.I18nHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * H2索引字段选择框。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellH2IndexFieldTextFiled extends ChooseTextField {

    private List<? extends H2Column> columnList;

    private List<String> columns;

    private PopupAdapter popup;

    public ShellH2IndexFieldTextFiled() {
    }

    /**
     * 构造索引字段选择框。
     *
     * @param columnList 可选字段列表
     * @param columns    已选字段列表
     */
    public ShellH2IndexFieldTextFiled(List<? extends H2Column> columnList, Collection<String> columns) {
        this.columnList = columnList;
        this.setColumns(new ArrayList<>(columns));
    }

    /**
     * 设置可选字段列表。
     *
     * @param columnList 可选字段列表
     */
    public void setColumnList(List<? extends H2Column> columnList) {
        this.columnList = columnList;
    }

    protected void initPopup() {
        this.disable();
        this.popup = PopupManager.parsePopup(ShellH2IndexFieldPopupController.class);
        this.popup.setProp("columns", this.columns);
        this.popup.setProp("columnList", this.columnList);
        this.popup.setProp("onSubmit", (Runnable) () -> {
            this.enable();
            this.skin().resetButtonColor();
            ShellH2IndexColumnListView listView = this.listView();
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
    public void setColumns(List<String> columns) {
        this.columns = columns == null ? new ArrayList<>() : columns;
        this.initText();
    }

    protected void initText() {
        String text = String.join(", ", this.columns);
        this.setText(text);
        this.setTipText(text);
    }

    protected ShellH2IndexColumnListView listView() {
        if (this.popup != null && this.popup.content() != null) {
            return (ShellH2IndexColumnListView) this.popup.content().lookup("#listView");
        }
        return null;
    }

    public List<String> getColumns() {
        return this.columns;
    }

    @Override
    public void initNode() {
        super.setAction(this::initPopup);
        this.setPromptText(I18nHelper.pleaseSelectField());
        super.initNode();
    }
}
