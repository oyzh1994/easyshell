package cn.oyzh.easyshell.fx.mariadb.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.popups.mariadb.ShellMariadbColumnFieldPopupController;
import cn.oyzh.fx.gui.text.field.ChooseTextField;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;
import cn.oyzh.i18n.I18nHelper;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * MariaDB字段选择输入框
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbFieldTextFiled extends ChooseTextField {

    /**
     * 构造字段选择输入框
     */
    public ShellMariadbFieldTextFiled() {
    }

    /**
     * 字段列表
     */
    private List<MariadbColumn> columns;

    /**
     * 已选中的字段名称集合
     */
    private Set<String> selectedColumns;

    /**
     * 构造字段选择输入框并初始化字段
     *
     * @param columns         字段列表
     * @param selectedColumns 已选中的字段名称集合
     */
    public ShellMariadbFieldTextFiled(List<MariadbColumn> columns, Set<String> selectedColumns) {
        this.columns = columns;
        this.setSelectedColumns(selectedColumns);
    }

    /**
     * 弹窗适配器
     */
    private PopupAdapter popup;

    /**
     * 初始化弹窗
     */
    protected void initPopup() {
        this.popup = PopupManager.parsePopup(ShellMariadbColumnFieldPopupController.class);
        this.popup.setProp("columns", this.columns);
        this.popup.setProp("selectedColumns", this.selectedColumns);
        this.popup.setProp("onSubmit", (Runnable) () -> {
            ShellMariadbColumnListView listView = this.listView();
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
    public void setColumns(List<MariadbColumn> columns) {
        this.columns = columns;
        ShellMariadbColumnListView listView = this.listView();
        if (listView != null) {
            listView.init(columns);
        }
        this.initText();
    }

    /**
     * 设置已选中的字段名称集合
     *
     * @param selectedColumns 已选中的字段名称集合
     */
    public void setSelectedColumns(Set<String> selectedColumns) {
        this.selectedColumns = selectedColumns;
        ShellMariadbColumnListView listView = this.listView();
        if (listView != null) {
            listView.select(selectedColumns);
        }
        this.initText();
    }

    /**
     * 获取已选中的字段名称集合
     *
     * @return 已选中的字段名称集合
     */
    public Set<String> getSelectedColumns() {
        return Objects.requireNonNullElse(this.selectedColumns, Collections.emptySet());
    }

    /**
     * 初始化输入框文本
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
     * 获取字段选择列表
     *
     * @return 字段选择列表
     */
    protected ShellMariadbColumnListView listView() {
        if (this.popup != null && this.popup.content() != null) {
            return (ShellMariadbColumnListView) this.popup.content().lookup("#listView");
        }
        return null;
    }

    @Override
    public void initNode() {
        super.setAction(this::initPopup);
        this.setPromptText(I18nHelper.pleaseSelectField());
        super.initNode();
    }
}
