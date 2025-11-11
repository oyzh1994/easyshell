package cn.oyzh.easyshell.fx.mysql.data;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.mysql.view.MysqlView;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.util.ListViewUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class ShellMysqlDataTransportViewListView extends FXListView<FXCheckBox> {

    private Runnable selectedChanged;

    public void of(List<MysqlView> views) {
        List<ShellMysqlDataTransportView> list = CollectionUtil.newArrayList();
        for (MysqlView view : views) {
            ShellMysqlDataTransportView obj = new ShellMysqlDataTransportView();
            obj.setName(view.getName());
            list.add(obj);
        }
        this.init(list);
    }

    public void init(List<ShellMysqlDataTransportView> views) {
        this.clearItems();
        if (CollectionUtil.isNotEmpty(views)) {
            for (ShellMysqlDataTransportView view : views) {
                FXCheckBox checkBox = new FXCheckBox();
                checkBox.setText(view.getName());
                checkBox.setSelected(view.isSelected());
                checkBox.setProp("data", view);
                checkBox.selectedChanged((observable, oldValue, newValue) -> {
                    view.setSelected(newValue);
                    if (this.selectedChanged != null) {
                        this.selectedChanged.run();
                    }
                });
                ListViewUtil.selectRowOnMouseClicked(checkBox);
                this.addItem(checkBox);
            }
        }
        if (this.selectedChanged != null) {
            this.selectedChanged.run();
        }
    }

    public List<ShellMysqlDataTransportView> getSelectedViews() {
        List<ShellMysqlDataTransportView> list = new ArrayList<>();
        for (FXCheckBox item : this.getItems()) {
            if (item.isSelected()) {
                list.add(item.getProp("data"));
            }
        }
        return list;
    }

    public int getSelectedSize() {
        int size = 0;
        for (FXCheckBox item : this.getItems()) {
            if (item.isSelected()) {
                size++;
            }
        }
        return size;
    }

    public Runnable getSelectedChanged() {
        return selectedChanged;
    }

    public void setSelectedChanged(Runnable selectedChanged) {
        this.selectedChanged = selectedChanged;
    }
}
