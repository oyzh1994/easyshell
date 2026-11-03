package cn.oyzh.easyshell.sqlite.check;

import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite 表检查设计组件。
 *
 * @since 2026-11-02
 */
public class SQLiteCheckControl extends SQLiteCheck {

    public ClearableTextField getNameControl() {
        ClearableTextField field = new ClearableTextField();
        field.setText(this.getName());
        field.setPromptText(I18nHelper.pleaseInputName());
        field.addTextChangeListener((observable, oldValue, newValue) -> {
            this.setName(newValue);
            this.setChanged(true);
        });
        TableViewUtil.rowOnCtrlS(field);
        TableViewUtil.selectRowOnMouseClicked(field);
        return field;
    }

    public ClearableTextField getClauseControl() {
        ClearableTextField field = new ClearableTextField();
        field.setText(this.getClause());
        field.setPromptText(I18nHelper.pleaseInputContent());
        field.addTextChangeListener((observable, oldValue, newValue) -> {
            this.setClause(newValue);
            this.setChanged(true);
        });
        TableViewUtil.rowOnCtrlS(field);
        TableViewUtil.selectRowOnMouseClicked(field);
        return field;
    }

    public static SQLiteCheckControl of(SQLiteCheck check) {
        SQLiteCheckControl control = new SQLiteCheckControl();
        control.copy(check);
        return control;
    }

    public static List<SQLiteCheckControl> of(List<SQLiteCheck> checks) {
        List<SQLiteCheckControl> controls = new ArrayList<>();
        if (checks != null) {
            for (SQLiteCheck check : checks) {
                controls.add(of(check));
            }
        }
        return controls;
    }
}
