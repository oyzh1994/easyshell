package cn.oyzh.easyshell.fx.split;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.list.FXListView;
import javafx.geometry.Insets;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-18
 */
public class ShellSplitListView extends FXListView<FXHBox> {

    {
        ShellConnectStore connectStore = ShellConnectStore.INSTANCE;
        List<ShellConnect> connects = connectStore.loadTermType();
        Insets insets1 = new Insets(10, 0, 0, 0);
        Insets insets2 = new Insets(11, 0, 0, 0);
        for (ShellConnect connect : connects) {
            FXLabel label = new FXLabel(connect.getName());
            FXCheckBox checkBox = new FXCheckBox();
            FXHBox hBox = new FXHBox(checkBox, label);
            hBox.setUserData(connect);
            hBox.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                checkBox.reversalSelected();
                event.consume();
            });
            HBox.setMargin(checkBox, insets1);
            HBox.setMargin(label, insets2);
            checkBox.selectedChanged((observable, oldValue, newValue) -> {
                this.checkMaxSelected();
            });
            this.addItem(hBox);
        }
    }

    /**
     * 限制最大选择数量
     */
    private int maxSelected = 2;

    public int getMaxSelected() {
        return maxSelected;
    }

    public void setMaxSelected(int maxSelected) {
        this.maxSelected = maxSelected;
    }

    /**
     * 检查最大选择数量
     */
    protected void checkMaxSelected() {
        // 未选择box
        List<FXHBox> list = new ArrayList<>();
        // 已选择总数
        int selected = 0;
        for (FXHBox item : this.getItems()) {
            FXCheckBox checkBox = (FXCheckBox) item.getFirstChild();
            if (checkBox.isSelected()) {
                selected++;
            } else {
                list.add(item);
            }
        }
        // 大于等于最大选择数量，禁用未选择的组件
        if (selected >= this.maxSelected) {
            for (FXHBox hBox : list) {
                hBox.disable();
            }
        } else {// 不大于则未选择组件启用
            for (FXHBox hBox : list) {
                hBox.enable();
            }
        }
    }

    /**
     * 全部取消选择
     */
    public void unSelectAll() {
        for (FXHBox item : this.getItems()) {
            FXCheckBox checkBox = (FXCheckBox) item.getFirstChild();
            checkBox.setSelected(false);
        }
    }

    /**
     * 获取已选择连接
     */
    public List<ShellConnect> getSelectedConnects() {
        List<ShellConnect> list = new ArrayList<>();
        for (FXHBox item : this.getItems()) {
            FXCheckBox checkBox = (FXCheckBox) item.getFirstChild();
            if (checkBox.isSelected()) {
                list.add((ShellConnect) item.getUserData());
            }
        }
        return list;
    }
}
