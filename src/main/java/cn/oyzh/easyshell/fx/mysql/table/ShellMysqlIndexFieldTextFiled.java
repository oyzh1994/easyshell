package cn.oyzh.easyshell.fx.mysql.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.index.MysqlIndex;
import cn.oyzh.easyshell.popups.mysql.ShellMysqlIndexFieldPopupController;
import cn.oyzh.fx.gui.text.field.ChooseTextField;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;
import cn.oyzh.i18n.I18nHelper;

import java.util.List;

/**
 * 索引字段文本框
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlIndexFieldTextFiled extends ChooseTextField {

    /**
     * 构造索引字段文本框
     */
    public ShellMysqlIndexFieldTextFiled() {
    }

    /**
     * 索引信息
     */
    private MysqlIndex dbIndex;

    /**
     * 可选字段列表
     */
    private List<MysqlColumn> columnList;

    /**
     * 已选索引字段列表
     */
    private List<MysqlIndex.IndexColumn> columns;

    /**
     * 构造索引字段文本框
     *
     * @param dbIndex    索引信息
     * @param columnList 可选字段列表
     * @param columns    已选索引字段列表
     */
    public ShellMysqlIndexFieldTextFiled(MysqlIndex dbIndex, List<MysqlColumn> columnList, List<MysqlIndex.IndexColumn> columns) {
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
        this.popup = PopupManager.parsePopup(ShellMysqlIndexFieldPopupController.class);
        this.popup.setProp("dbIndex", this.dbIndex);
        this.popup.setProp("columns", this.columns);
        this.popup.setProp("columnList", this.columnList);
        this.popup.setProp("onSubmit", (Runnable) () -> {
            this.enable();
            this.skin().resetButtonColor();
            ShellMysqlIndexColumnListView listView = this.listView();
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
    public void setColumns(List<MysqlIndex.IndexColumn> columns) {
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
            for (MysqlIndex.IndexColumn column : this.columns) {
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
    protected ShellMysqlIndexColumnListView listView() {
        if (this.popup != null && this.popup.content() != null) {
            return (ShellMysqlIndexColumnListView) this.popup.content().lookup("#listView");
        }
        return null;
    }

    /**
     * 获取已选索引字段列表
     *
     * @return 已选索引字段列表
     */
    public List<MysqlIndex.IndexColumn> getColumns() {
        return columns;
    }

    @Override
    public void initNode() {
        super.setAction(this::initPopup);
        this.setPromptText(I18nHelper.pleaseSelectField());
        super.initNode();
    }
}
