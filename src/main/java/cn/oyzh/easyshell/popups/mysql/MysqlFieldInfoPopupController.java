package cn.oyzh.easyshell.popups.mysql;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.util.mysql.DBNodeUtil;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.PopupController;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupAttribute;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.WindowEvent;

import java.util.List;

/**
 * 字段信息弹窗业务
 *
 * @author oyzh
 * @since 2024/07/26
 */
@PopupAttribute(
        value = FXConst.POPUP_PATH + "mysql/shellMysqlFieldInfoPopup.fxml"
)
public class MysqlFieldInfoPopupController extends PopupController {

    /**
     * 名称
     */
    @FXML
    private TextField name;

    /**
     * 字段长
     */
    @FXML
    private NumberTextField size;

    /**
     * 类型
     */
    @FXML
    private TextField type;

    /**
     * 值
     */
    @FXML
    private TextField value;

    /**
     * 注释
     */
    @FXML
    private TextArea comment;

    /**
     * 默认值
     */
    @FXML
    private TextField defaultValue;

    /**
     * 长度组件
     */
    @FXML
    private FXHBox sizeBox;

    /**
     * 标签组件
     */
    @FXML
    private FXHBox tagsBox;

    /**
     * 值组件
     */
    @FXML
    private FXHBox valueBox;

    /**
     * 默认值值组件
     */
    @FXML
    private FXHBox defaultValueBox;

    /**
     * 关闭
     */
    @FXML
    private void close() {
        this.closeWindow();
    }

    @Override
    public void onWindowShowing(WindowEvent event) {
        super.onWindowShowing(event);
        MysqlColumn column = this.getProp("column");
        if (column.supportSize()) {
            if (column.getSize() != null) {
                this.size.setValue(column.getSize());
            }
            this.sizeBox.display();
        }
        if (column.supportValue()) {
            this.value.setText(column.getValue());
            this.valueBox.display();
        }
        if (column.supportDefaultValue()) {
            this.defaultValue.setText(column.getDefaultValueString());
            this.defaultValueBox.display();
        }
        List<FXLabel> tags = DBNodeUtil.generateTags(column);
        if (CollectionUtil.isNotEmpty(tags)) {
            this.tagsBox.addChild(tags);
            this.tagsBox.display();
            boolean first = true;
            for (FXLabel tag : tags) {
                if (first) {
                    first = false;
                    HBox.setMargin(tag, new Insets(5, 0, 0, 10));
                } else {
                    HBox.setMargin(tag, new Insets(5, 0, 0, 5));
                }
            }
        }
        this.name.setText(column.getName());
        this.type.setText(column.getType());
        this.comment.setText(column.getComment());
    }

    @Override
    public void onPopupInitialize(PopupAdapter window) {
        super.onPopupInitialize(window);
        this.tagsBox.managedBindVisible();
        this.sizeBox.managedBindVisible();
        this.valueBox.managedBindVisible();
        this.defaultValueBox.managedBindVisible();
    }
}
