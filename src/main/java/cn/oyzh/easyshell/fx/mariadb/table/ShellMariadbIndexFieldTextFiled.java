package cn.oyzh.easyshell.fx.mariadb.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.index.MariadbIndex;
import cn.oyzh.easyshell.popups.mariadb.ShellMariadbIndexFieldPopupController;
import cn.oyzh.fx.gui.text.field.ChooseTextField;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;
import cn.oyzh.i18n.I18nHelper;

import java.util.List;

/**
 * 索引字段文本框
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbIndexFieldTextFiled extends ChooseTextField {

    /**
     * 构造索引字段文本框
     */
    public ShellMariadbIndexFieldTextFiled() {
    }

    /**
     * 索引信息
     */
    private MariadbIndex dbIndex;

    /**
     * 可选字段列表
     */
    private List<MariadbColumn> columnList;

    /**
     * 已选索引字段列表
     */
    private List<MariadbIndex.IndexColumn> columns;

    /**
     * 构造索引字段文本框
     *
     * @param dbIndex    索引信息
     * @param columnList 可选字段列表
     * @param columns    已选索引字段列表
     */
    public ShellMariadbIndexFieldTextFiled(MariadbIndex dbIndex, List<MariadbColumn> columnList, List<MariadbIndex.IndexColumn> columns) {
        this.dbIndex = dbIndex;
        this.columnList = columnList;
        this.setColumns(columns);
    }

    /**
     * 字段选择弹窗
     */
    private PopupAdapter popup;

    /**
     * 初始化字段选择弹窗
     */
    protected void initPopup() {
        this.disable();
        this.popup = PopupManager.parsePopup(ShellMariadbIndexFieldPopupController.class);
        this.popup.setProp("dbIndex", this.dbIndex);
        this.popup.setProp("columns", this.columns);
        this.popup.setProp("columnList", this.columnList);
        this.popup.setProp("onSubmit", (Runnable) () -> {
            this.enable();
            this.skin().resetButtonColor();
            ShellMariadbIndexColumnListView listView = this.listView();
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
     * 设置已选索引字段列表
     *
     * @param columns 已选索引字段列表
     */
    public void setColumns(List<MariadbIndex.IndexColumn> columns) {
        this.columns = columns;
        this.initText();
    }

    /**
     * 初始化文本内容
     */
    protected void initText() {
        String text;
        StringBuilder builder = new StringBuilder();
        if (CollectionUtil.isNotEmpty(this.columns)) {
            for (MariadbIndex.IndexColumn column : this.columns) {
                builder.append(",");
                builder.append(column.getColumnName());
                if (column.getSubPart() != null && column.getSubPart() > 0) {
                    builder.append("(").append(column.getSubPart()).append(")");
                }
            }
            text = builder.substring(1);
        } else {
            text = "";
        }
        this.setText(text);
        this.setTipText(text);
    }

    /**
     * 获取索引字段选择列表
     *
     * @return 索引字段选择列表
     */
    protected ShellMariadbIndexColumnListView listView() {
        if (this.popup != null && this.popup.content() != null) {
            return (ShellMariadbIndexColumnListView) this.popup.content().lookup("#listView");
        }
        return null;
    }

    /**
     * 获取已选索引字段列表
     *
     * @return 已选索引字段列表
     */
    public List<MariadbIndex.IndexColumn> getColumns() {
        return columns;
    }

    @Override
    public void initNode() {
        super.setAction(this::initPopup);
        this.setPromptText(I18nHelper.pleaseSelectField());
        super.initNode();
    }
}
