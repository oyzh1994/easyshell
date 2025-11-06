package cn.oyzh.easyshell.popups.mysql;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.PopupController;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.ControlUtil;
import cn.oyzh.fx.plus.util.ListViewUtil;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupAttribute;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import java.util.List;

/**
 * 字段列表弹窗业务
 *
 * @author oyzh
 * @since 2024/07/12
 */
@PopupAttribute(
        value = FXConst.POPUP_PATH + "mysql/shellMysqlColumnEnumPopup.fxml"
)
public class MysqlColumnEnumPopupController extends PopupController {

    /**
     * 提交事件
     */
    private Runnable onSubmit;

    /**
     * 值组件
     */
    @FXML
    private FXListView<ClearableTextField> listView;

    /**
     * 提交
     */
    @FXML
    private void submit() {
        try {
            if (this.onSubmit != null) {
                this.onSubmit.run();
            }
            this.closeWindow();
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 关闭
     */
    @FXML
    private void close() {
        this.closeWindow();
    }

    /**
     * 添加行
     */
    @FXML
    private void addRow() {
        this.listView.addItem(this.createNode(""));
        this.listView.selectLast();
    }

    /**
     * 删除行
     */
    @FXML
    private void deleteRow() {
        this.listView.removeSelectedItem();
    }

    private ClearableTextField createNode(String text) {
        ClearableTextField textField = new ClearableTextField(text);
        textField.setRealHeight(22);
        textField.setRealWidth(100);
        textField.setFlexWidth("100% - 20");
        textField.setBorder(ControlUtil.strokeOfWidthBottom(Color.GRAY, 0.5));
        ListViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
    }

    @Override
    public void onWindowShowing(WindowEvent event) {
        super.onWindowShowing(event);
        this.onSubmit = this.getProp("onSubmit");
        List<String> values = this.getProp("values");
        if (CollectionUtil.isNotEmpty(values)) {
            for (String value : values) {
                this.listView.addItem(this.createNode(value));
            }
        }
    }

    @Override
    public void onPopupInitialize(PopupAdapter window) {
        super.onPopupInitialize(window);
        this.listView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<ClearableTextField> call(ListView<ClearableTextField> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(ClearableTextField item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            this.setGraphic(null);
                        } else {
                            this.setGraphic(item);
                            this.setPrefHeight(25);
                            ListViewUtil.highlightCell(this);
                        }
                    }
                };
            }
        });
    }
}
