package cn.oyzh.easyshell.fx.dameng.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.popups.dameng.ShellDamengColumnFieldPopupController;
import cn.oyzh.fx.gui.text.field.ChooseTextField;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;
import cn.oyzh.i18n.I18nHelper;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 达梦字段选择框
 *
 * @author oyzh
 * @since 2024/7/10
 */
public class DamengFieldTextFiled extends ChooseTextField {

    {
        super.setAction(this::initPopup);
        this.setPromptText(I18nHelper.pleaseSelectField());
    }

    /**
     * 构造达梦字段选择框
     */
    public DamengFieldTextFiled() {
    }

    /**
     * 字段列表
     */
    private List<DamengColumn> columns;

    /**
     * 已选中的字段名称列表
     */
    private List<String> selectedColumns;

    /**
     * 构造并初始化达梦字段选择框
     *
     * @param columns         字段列表
     * @param selectedColumns 已选中的字段名称列表
     */
    public DamengFieldTextFiled(List<DamengColumn> columns, List<String> selectedColumns) {
        this.columns = columns;
        this.setSelectedColumns(selectedColumns);
    }

    /**
     * 弹出选择框
     */
    private PopupAdapter popup;

    /**
     * 初始化并弹出字段选择框
     */
    protected void initPopup() {
        this.popup = PopupManager.parsePopup(ShellDamengColumnFieldPopupController.class);
        this.popup.setProp("columns", this.columns);
        this.popup.setProp("selectedColumns", this.selectedColumns);
        this.popup.setProp("onSubmit", (Runnable) () -> {
            DamengColumnListView listView = this.listView();
            if (listView != null) {
                this.selectedColumns = listView.getSelectedColumnNames();
            }
            this.initText();
        });
        this.popup.showPopup(this);
    }

    /**
     * 设置字段列表
     *
     * @param columns 字段列表
     */
    public void setColumns(List<DamengColumn> columns) {
        this.columns = columns;
        DamengColumnListView listView = this.listView();
        if (listView != null) {
            listView.init(columns);
        }
        this.initText();
    }

    /**
     * 设置已选中的字段
     *
     * @param selectedColumns 已选中的字段名称列表
     */
    public void setSelectedColumns(List<String> selectedColumns) {
        this.selectedColumns = selectedColumns;
        DamengColumnListView listView = this.listView();
        if (listView != null) {
            listView.select(selectedColumns);
        }
        this.initText();
    }

    /**
     * 获取已选中的字段名称列表
     *
     * @return 已选中的字段名称列表
     */
    public List<String> getSelectedColumns() {
        return Objects.requireNonNullElse(this.selectedColumns, Collections.emptyList());
    }

    /**
     * 根据已选中的字段刷新文本显示
     */
    protected void initText() {
        String text = "";
        if (CollectionUtil.isNotEmpty(this.selectedColumns)) {
            text = CollectionUtil.join(this.selectedColumns, ",");
        }
        this.setText(text);
        this.setTipText(text);
    }

    /**
     * 获取弹出框内的字段多选列表
     *
     * @return 字段多选列表，弹出框未初始化时返回 null
     */
    protected DamengColumnListView listView() {
        if (this.popup != null && this.popup.content() != null) {
            return (DamengColumnListView) this.popup.content().lookup("#listView");
        }
        return null;
    }
}
