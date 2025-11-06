package cn.oyzh.easyshell.fx.mysql.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.index.MysqlIndex;
import cn.oyzh.easyshell.popups.mysql.MysqlIndexFieldPopupController;
import cn.oyzh.fx.gui.text.field.ChooseTextField;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;
import cn.oyzh.i18n.I18nHelper;

import java.util.List;

/**
 * @author oyzh
 * @since 2024/7/16
 */
public class MysqlIndexFieldTextFiled extends ChooseTextField {

    {
        super.setAction(this::initPopup);
        this.setPromptText(I18nHelper.pleaseSelectField());
    }

    public MysqlIndexFieldTextFiled() {
    }

    private MysqlIndex dbIndex;

    private List<MysqlColumn> columnList;

    private List<MysqlIndex.IndexColumn> columns;

    public MysqlIndexFieldTextFiled(MysqlIndex dbIndex, List<MysqlColumn> columnList, List<MysqlIndex.IndexColumn> columns) {
        this.dbIndex = dbIndex;
        this.columnList = columnList;
        this.setColumns(columns);
    }

    private PopupAdapter popup;

    protected void initPopup() {
        this.disable();
        this.popup = PopupManager.parsePopup(MysqlIndexFieldPopupController.class);
        this.popup.setProp("dbIndex", this.dbIndex);
        this.popup.setProp("columns", this.columns);
        this.popup.setProp("columnList", this.columnList);
        this.popup.setProp("onSubmit", (Runnable) () -> {
            this.enable();
            this.skin().resetButtonColor();
            MysqlIndexColumnListView listView = this.listView();
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

    public void setColumns(List<MysqlIndex.IndexColumn> columns) {
        this.columns = columns;
        this.initText();
    }

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

    protected MysqlIndexColumnListView listView() {
        if (this.popup != null && this.popup.content() != null) {
            return (MysqlIndexColumnListView) this.popup.content().lookup("#listView");
        }
        return null;
    }

    public List<MysqlIndex.IndexColumn> getColumns() {
        return columns;
    }
}
