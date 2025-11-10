package cn.oyzh.easyshell.fx.mysql.table;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.popups.mysql.ShellMysqlColumnEnumPopupController;
import cn.oyzh.fx.gui.text.field.ChooseTextField;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;
import cn.oyzh.i18n.I18nHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/7/10
 */
public class DBEnumTextFiled extends ChooseTextField {

    {
        super.setAction(this::initPopup);
        this.setPromptText(I18nHelper.pleaseSelectContent());
    }

    private List<String> values;

    public DBEnumTextFiled() {
    }

    public DBEnumTextFiled(List<String> values) {
        this.values = values;
    }

    private PopupAdapter popup;

    protected void initPopup() {
        this.popup = PopupManager.parsePopup(ShellMysqlColumnEnumPopupController.class);
        this.popup.setProp("values", this.values);
        this.popup.setProp("onSubmit", (Runnable) () -> {
            FXListView<ClearableTextField> listView = this.listView();
            if (listView != null) {
                this.values = new ArrayList<>();
                for (ClearableTextField item : listView.getItems()) {
                    this.values.add(item.getTextTrim());
                }
            }
            this.initText();
        });
        this.popup.showPopup(this);
    }

    public void initText() {
        if (CollectionUtil.isEmpty(this.values)) {
            this.setText("");
        } else {
            StringBuilder builder = new StringBuilder();
            for (String value : this.values) {
                builder.append(",").append("'").append(value).append("'");
            }
            this.setText(builder.substring(1));
        }
    }

    public void setValues(List<String> values) {
        this.values = values;
        FXListView listView = this.listView();
        if (listView != null) {
            listView.setItem(values);
        }
        this.initText();
    }

    protected FXListView<ClearableTextField> listView() {
        if (this.popup != null) {
            return (FXListView<ClearableTextField>) this.popup.content().lookup("#listView");
        }
        return null;
    }
}
