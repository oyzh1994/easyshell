package cn.oyzh.easyshell.sqlite.index;

import cn.oyzh.fx.editor.incubator.EditorFormatType;
import cn.oyzh.fx.editor.incubator.control.EditorEnlargeTextFiled;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite 索引设计组件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteIndexControl extends SQLiteIndex {

    public ClearableTextField getNameControl() {
        ClearableTextField field = new ClearableTextField();
        field.setPromptText(I18nHelper.pleaseInputName());
        field.setText(this.getName());
        field.addTextChangeListener((observable, oldValue, newValue) -> {
            this.setName(newValue);
            this.setChanged(true);
        });
        TableViewUtil.rowOnCtrlS(field);
        TableViewUtil.selectRowOnMouseClicked(field);
        return field;
    }

    public EditorEnlargeTextFiled getDefinitionControl() {
        EditorEnlargeTextFiled field = new EditorEnlargeTextFiled();
        field.setFormatType(EditorFormatType.SQL);
        field.setPromptText(I18nHelper.pleaseInputContent());
        field.setText(this.getDefinition());
        field.addTextChangeListener((observable, oldValue, newValue) -> {
            this.setDefinition(newValue);
            this.setChanged(true);
        });
        TableViewUtil.rowOnCtrlS(field);
        TableViewUtil.selectRowOnMouseClicked(field);
        return field;
    }

    public static SQLiteIndexControl of(SQLiteIndex index) {
        SQLiteIndexControl control = new SQLiteIndexControl();
        control.copy(index);
        return control;
    }

    public static List<SQLiteIndexControl> of(List<SQLiteIndex> indexes) {
        List<SQLiteIndexControl> controls = new ArrayList<>();
        if (indexes != null) {
            for (SQLiteIndex index : indexes) {
                controls.add(of(index));
            }
        }
        return controls;
    }
}
