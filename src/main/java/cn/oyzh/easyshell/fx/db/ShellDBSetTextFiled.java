package cn.oyzh.easyshell.fx.db;

import cn.oyzh.easyshell.popups.db.ShellDBSetPopupController;
import cn.oyzh.easyshell.util.db.ShellDBSetValueUtil;
import cn.oyzh.fx.gui.text.field.ChooseTextField;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;
import cn.oyzh.i18n.I18nHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 数据库 SET 字段多选输入框。
 *
 * <p>选项使用复选弹窗，选中值按 {@code value1,value2} 格式回填。</p>
 */
public class ShellDBSetTextFiled extends ChooseTextField {

    /**
     * 可选值
     */
    private List<String> items = List.of();

    /**
     * 已选值
     */
    private List<String> selectedValues = List.of();

    /**
     * 选择弹窗
     */
    private PopupAdapter popup;

    /**
     * 构造器
     */
    public ShellDBSetTextFiled() {
    }

    /**
     * 构造器
     *
     * @param items 可选值
     */
    public ShellDBSetTextFiled(List<String> items) {
        this.setItems(items);
    }

    /**
     * 设置可选值
     *
     * @param items 可选值
     */
    public void setItems(List<String> items) {
        this.items = items == null ? List.of() : new ArrayList<>(items);
    }

    /**
     * 获取可选值
     *
     * @return 可选值
     */
    public List<String> getItems() {
        return this.items;
    }

    /**
     * 设置字段值。
     *
     * @param value 逗号分隔的 SET 值
     */
    public void setValue(Object value) {
        this.setSelectedValues(parseValues(value));
    }

    /**
     * 获取字段值。
     *
     * @return 逗号分隔的 SET 值
     */
    public String getValue() {
        return joinValues(this.selectedValues);
    }

    /**
     * 设置选中值
     *
     * @param values 选中值
     */
    public void setSelectedValues(List<String> values) {
        this.selectedValues = values == null ? List.of() : new ArrayList<>(values);
        this.initText();
    }

    /**
     * 获取选中值
     *
     * @return 选中值
     */
    public List<String> getSelectedValues() {
        return new ArrayList<>(this.selectedValues);
    }

    /**
     * 初始化选择弹窗。
     */
    private void initPopup() {
        this.popup = PopupManager.parsePopup(ShellDBSetPopupController.class);
        this.popup.setProp("items", new ArrayList<>(this.items));
        this.popup.setProp("selectedValues", new ArrayList<>(this.selectedValues));
        this.popup.setProp("onSubmit", (Consumer<List<String>>) values -> {
            this.selectedValues = values == null ? List.of() : new ArrayList<>(values);
            this.initText();
        });
        this.popup.showPopup(this);
    }

    /**
     * 更新显示文本。
     */
    private void initText() {
        this.setText(joinValues(this.selectedValues));
    }

    /**
     * 解析 SET 值。
     *
     * @param value SET 值
     * @return 值列表
     */
    public static List<String> parseValues(Object value) {
        return ShellDBSetValueUtil.parseValues(value);
    }

    /**
     * 拼接 SET 值。
     *
     * @param values 值列表
     * @return 逗号分隔的 SET 值
     */
    public static String joinValues(List<String> values) {
        return ShellDBSetValueUtil.joinValues(values);
    }

    @Override
    public void initNode() {
        super.setAction(this::initPopup);
        this.setPromptText(I18nHelper.pleaseSelectContent());
        super.initNode();
    }
}
