package cn.oyzh.easyshell.mysql.check;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.util.mysql.DBUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/11
 */
public class MysqlCheckControl extends MysqlCheck {

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

    public static MysqlCheckControl of(MysqlCheck check) {
        MysqlCheckControl control = new MysqlCheckControl();
        control.copy(check);
        return control;
    }

    public static List<MysqlCheckControl> of(List<MysqlCheck> checks) {
        List<MysqlCheckControl> controls = new ArrayList<>();
        for (MysqlCheck check : checks) {
            controls.add(of(check));
        }
        return controls;
    }
}
