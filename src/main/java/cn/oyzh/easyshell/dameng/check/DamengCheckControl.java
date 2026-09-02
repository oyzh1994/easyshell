package cn.oyzh.easyshell.dameng.check;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/11
 */
public class DamengCheckControl extends DamengCheck {

    public ClearableTextField getNameControl() {
        ClearableTextField textField = new ClearableTextField();
        textField.setPromptText(I18nHelper.pleaseInputName());
        if (StringUtil.isEmpty(this.getName())) {
            this.setName(DBUtil.genCheckName());
        }
        textField.addTextChangeListener((observable, oldValue, newValue) -> this.setName(newValue));
        textField.setText(this.getName());
        TableViewUtil.rowOnCtrlS(textField);
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    public ClearableTextField getClauseControl() {
        ClearableTextField textField = new ClearableTextField();
        textField.setPromptText(I18nHelper.pleaseInputName());
        textField.addTextChangeListener((observable, oldValue, newValue) -> this.setClause(newValue));
        textField.setText(this.getClause());
        TableViewUtil.rowOnCtrlS(textField);
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    public static DamengCheckControl of(DamengCheck check) {
        DamengCheckControl control = new DamengCheckControl();
        control.copy(check);
        return control;
    }

    public static List<DamengCheckControl> of(List<DamengCheck> checks) {
        List<DamengCheckControl> controls = new ArrayList<>();
        for (DamengCheck check : checks) {
            controls.add(of(check));
        }
        return controls;
    }
}
