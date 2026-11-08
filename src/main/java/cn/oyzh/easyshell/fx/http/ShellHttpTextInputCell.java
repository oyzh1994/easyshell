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
            if (this.updating) {
                return;
            }
            ShellHttpKeyValue row = this.row();
            if (row != null) {
                this.setter.accept(row, newValue);
            }
        });
    }

    /**
     * 获取当前单元格对应的参数行。
     *
     * @return 参数行
     */
    private ShellHttpKeyValue row() {
        if (this.getTableRow() != null && this.getTableRow().getItem() != null) {
            return this.getTableRow().getItem();
        }
        if (this.getIndex() >= 0 && this.getIndex() < this.getTableView().getItems().size()) {
            return this.getTableView().getItems().get(this.getIndex());
        }
        return null;
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        ShellHttpKeyValue row = this.row();
        if (empty || row == null) {
            this.setGraphic(null);
            return;
        }
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
