package cn.oyzh.easyshell.fx.http;

import cn.oyzh.easyshell.http.ShellHttpKeyValue;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * HTTP 键值表格文本输入单元格。
 *
 * @author oyzh
 * @since 2026-11-08
 */
public class ShellHttpTextInputCell extends TableCell<ShellHttpKeyValue, String> {

    /**
     * 文本输入框
     */
    private final ClearableTextField textField = new ClearableTextField();

    /**
     * 值读取
     */
    private final Function<ShellHttpKeyValue, String> getter;

    /**
     * 值写入
     */
    private final BiConsumer<ShellHttpKeyValue, String> setter;

    /**
     * 是否正在更新
     */
    private boolean updating;

    /**
     * 构造文本输入单元格
     *
     * @param getter     值读取
     * @param setter     值写入
     * @param promptText 背景文字
     */
    public ShellHttpTextInputCell(Function<ShellHttpKeyValue, String> getter,
                                  BiConsumer<ShellHttpKeyValue, String> setter,
                                  String promptText) {
        this.getter = getter;
        this.setter = setter;
        this.textField.setPromptText(promptText);
        this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        this.textField.setRealHeight(26);
        this.textField.prefWidthProperty().bind(this.widthProperty().subtract(6));
        // 点击输入框时同步选中所在表格行，保证删除等行操作可用
        TableViewUtil.selectRowOnMouseClicked(this.textField);
        this.textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (this.updating || this.getTableRow() == null) {
                return;
            }
            ShellHttpKeyValue row = this.getTableRow().getItem();
            if (row != null) {
                this.setter.accept(row, newValue);
            }
        });
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || this.getTableRow() == null || this.getTableRow().getItem() == null) {
            this.setGraphic(null);
            return;
        }
        ShellHttpKeyValue row = this.getTableRow().getItem();
        String value = this.getter.apply(row);
        // 输入过程中属性会回写并触发 updateItem，文本相同则不能重设，否则光标会跳到开头
        if (!this.textField.isFocused() && !Objects.equals(this.textField.getText(), value)) {
            this.updating = true;
            this.textField.setText(value);
            this.updating = false;
        }
        if (this.getGraphic() != this.textField) {
            this.setGraphic(this.textField);
        }
    }
}
