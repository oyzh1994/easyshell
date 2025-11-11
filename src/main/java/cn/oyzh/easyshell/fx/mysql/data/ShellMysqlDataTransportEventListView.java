package cn.oyzh.easyshell.fx.mysql.data;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.mysql.event.MysqlEvent;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.util.ListViewUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class ShellMysqlDataTransportEventListView extends FXListView<FXCheckBox> {

    private Runnable selectedChanged;

    public void of(List<MysqlEvent> events) {
        List<ShellMysqlDataTransportEvent> list = CollectionUtil.newArrayList();
        for (MysqlEvent event : events) {
            ShellMysqlDataTransportEvent obj = new ShellMysqlDataTransportEvent();
            obj.setName(event.getName());
            list.add(obj);
        }
        this.init(list);
    }

    public void init(List<ShellMysqlDataTransportEvent> events) {
        this.clearItems();
        if (CollectionUtil.isNotEmpty(events)) {
            for (ShellMysqlDataTransportEvent event : events) {
                FXCheckBox checkBox = new FXCheckBox();
                checkBox.setText(event.getName());
                checkBox.setSelected(event.isSelected());
                checkBox.setProp("data", event);
                checkBox.selectedChanged((observable, oldValue, newValue) -> {
                    event.setSelected(newValue);
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

    public List<ShellMysqlDataTransportEvent> getSelectedEvents() {
        List<ShellMysqlDataTransportEvent> list = new ArrayList<>();
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
