package cn.oyzh.easyshell.popups.db;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.PopupController;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.ListViewUtil;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupAttribute;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 数据库 SET 字段多选弹窗。
 */
@PopupAttribute(
        value = FXConst.POPUP_PATH + "db/shellDBSetPopup.fxml"
)
public class ShellDBSetPopupController extends PopupController {

    /**
     * 提交回调
     */
    private Consumer<List<String>> onSubmit;

    /**
     * 复选项列表
     */
    @FXML
    private FXListView<FXCheckBox> listView;

    /**
     * 提交
     */
    @FXML
    private void submit() {
        try {
            List<String> values = new ArrayList<>();
            for (FXCheckBox checkBox : this.listView.getItems()) {
                if (checkBox.isSelected()) {
                    values.add(checkBox.getText());
                }
            }
            if (this.onSubmit != null) {
                this.onSubmit.accept(values);
            }
            this.closeWindow();
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

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
        this.onSubmit = this.getProp("onSubmit");
        this.listView.clearItems();
        List<String> items = this.getProp("items");
        List<String> selectedValues = this.getProp("selectedValues");
        if (CollectionUtil.isNotEmpty(items)) {
            for (String item : items) {
                FXCheckBox checkBox = new FXCheckBox(item,
                        CollectionUtil.isNotEmpty(selectedValues) && selectedValues.contains(item));
                checkBox.setPrefHeight(22);
                this.listView.addItem(checkBox);
            }
        }
    }

    @Override
    public void onPopupInitialize(PopupAdapter window) {
        super.onPopupInitialize(window);
        this.listView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<FXCheckBox> call(ListView<FXCheckBox> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(FXCheckBox item, boolean empty) {
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
