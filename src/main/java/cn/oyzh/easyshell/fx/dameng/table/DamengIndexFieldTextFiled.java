package cn.oyzh.easyshell.fx.dameng.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.index.DamengIndex;
import cn.oyzh.easyshell.popups.dameng.DamengIndexFieldPopupController;
import cn.oyzh.fx.gui.text.field.ChooseTextField;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;
import cn.oyzh.i18n.I18nHelper;

import java.util.List;

/**
 * @author oyzh
 * @since 2024/7/16
 */
public class DamengIndexFieldTextFiled extends ChooseTextField {

    {
        super.setAction(this::initPopup);
        this.setPromptText(I18nHelper.pleaseSelectField());
    }

    public DamengIndexFieldTextFiled() {
    }

    private DamengIndex dbIndex;

    private List<DamengColumn> columnList;

    private List<DamengIndex.IndexColumn> columns;

    public DamengIndexFieldTextFiled(DamengIndex dbIndex, List<DamengColumn> columnList, List<DamengIndex.IndexColumn> columns) {
        this.dbIndex = dbIndex;
        this.columnList = columnList;
        this.setColumns(columns);
    }

    private PopupAdapter popup;

    protected void initPopup() {
        this.disable();
        this.popup = PopupManager.parsePopup(DamengIndexFieldPopupController.class);
        this.popup.setProp("dbIndex", this.dbIndex);
        this.popup.setProp("columns", this.columns);
        this.popup.setProp("columnList", this.columnList);
        this.popup.setProp("onSubmit", (Runnable) () -> {
            this.enable();
            this.skin().resetButtonColor();
            DamengIndexColumnListView listView = this.listView();
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

    public void setColumns(List<DamengIndex.IndexColumn> columns) {
        this.columns = columns;
        this.initText();
    }

    protected void initText() {
        String text;
        StringBuilder builder = new StringBuilder();
        if (CollectionUtil.isNotEmpty(this.columns)) {
            for (DamengIndex.IndexColumn column : this.columns) {
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

    protected DamengIndexColumnListView listView() {
        if (this.popup != null && this.popup.content() != null) {
            return (DamengIndexColumnListView) this.popup.content().lookup("#listView");
        }
        return null;
    }

    public List<DamengIndex.IndexColumn> getColumns() {
        return columns;
    }
}
