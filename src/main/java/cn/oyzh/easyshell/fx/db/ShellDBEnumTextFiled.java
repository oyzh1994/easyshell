package cn.oyzh.easyshell.fx.db;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.popups.db.ShellDBColumnEnumPopupController;
import cn.oyzh.fx.gui.text.field.ChooseTextField;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;
import cn.oyzh.i18n.I18nHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库字段枚举值输入框
 *
 * @author oyzh
 * @since 2024/7/10
 */
public class ShellDBEnumTextFiled extends ChooseTextField {

    /**
     * 枚举值列表
     */
    private List<String> values;

    /**
     * 构造器
     */
    public ShellDBEnumTextFiled() {
    }

    /**
     * 构造器
     *
     * @param values 枚举值列表
     */
    public ShellDBEnumTextFiled(List<String> values) {
        this.values = values;
    }

    /**
     * 枚举值选择弹窗
     */
    private PopupAdapter popup;

    /**
     * 初始化枚举值选择弹窗
     */
    protected void initPopup() {
        this.popup = PopupManager.parsePopup(ShellDBColumnEnumPopupController.class);
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

    /**
     * 根据枚举值列表初始化文本
     */
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

    /**
     * 设置枚举值列表
     *
     * @param values 枚举值列表
     */
    public void setValues(List<String> values) {
        this.values = values;
        FXListView listView = this.listView();
        if (listView != null) {
            listView.setItem(values);
        }
        this.initText();
    }

    /**
     * 获取弹窗中的枚举值列表视图
     *
     * @return 枚举值列表视图
     */
    protected FXListView<ClearableTextField> listView() {
        if (this.popup != null && this.popup.content() != null) {
            return (FXListView<ClearableTextField>) this.popup.content().lookup("#listView");
        }
        return null;
    }

    @Override
    public void initNode() {
        super.setAction(this::initPopup);
        this.setPromptText(I18nHelper.pleaseSelectContent());
        super.initNode();
    }
}
