package cn.oyzh.easyshell.data.db.ui;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.data.db.dto.DBDataTransportObject;
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
public class DBDataTransportObjectListView extends FXListView<FXCheckBox> {

    protected Runnable selectedChanged;

    public void init(List<DBDataTransportObject> events) {
        this.clearItems();
        if (CollectionUtil.isNotEmpty(events)) {
            for (DBDataTransportObject event : events) {
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

    public List<DBDataTransportObject> getSelectedObjects() {
        List<DBDataTransportObject> list = new ArrayList<>();
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
