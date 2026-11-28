package cn.oyzh.easyshell.h2.check;

import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * H2 表检查设计组件。
 *
 * @since 2026-11-02
 */
public class H2CheckControl extends H2Check {

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

    public static H2CheckControl of(H2Check check) {
        H2CheckControl control = new H2CheckControl();
        control.copy(check);
        return control;
    }

    public static List<H2CheckControl> of(List<H2Check> checks) {
        List<H2CheckControl> controls = new ArrayList<>();
        if (checks != null) {
            for (H2Check check : checks) {
                controls.add(of(check));
            }
        }
        return controls;
    }
}
