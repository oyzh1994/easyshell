package cn.oyzh.easyshell.fx.mysql.data;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.fx.mysql.data.DataTransportTrigger;
import cn.oyzh.easyshell.mysql.trigger.MysqlTrigger;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.util.ListViewUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class DataTransportTriggerListView extends FXListView<FXCheckBox> {

    private Runnable selectedChanged;

    public void of(List<MysqlTrigger> triggers) {
        List<DataTransportTrigger> list = CollectionUtil.newArrayList();
        for (MysqlTrigger trigger : triggers) {
            DataTransportTrigger obj = new DataTransportTrigger();
            obj.setName(trigger.getName());
            list.add(obj);
        }
        this.init(list);
    }

    public void init(List<DataTransportTrigger> triggers) {
        this.clearItems();
        if (CollectionUtil.isNotEmpty(triggers)) {
            for (DataTransportTrigger trigger : triggers) {
                FXCheckBox checkBox = new FXCheckBox();
                checkBox.setText(trigger.getName());
                checkBox.setSelected(trigger.isSelected());
                checkBox.setProp("data", trigger);
                checkBox.selectedChanged((observable, oldValue, newValue) -> {
                    trigger.setSelected(newValue);
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

    public List<DataTransportTrigger> getSelectedTriggers() {
        List<DataTransportTrigger> list = new ArrayList<>();
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
