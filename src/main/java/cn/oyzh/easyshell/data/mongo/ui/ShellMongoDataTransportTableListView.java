package cn.oyzh.easyshell.data.mongo.ui;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.data.mongo.dto.ShellMongoDataTransportCollection;
import cn.oyzh.easyshell.mongo.MongoCollection;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.util.ListViewUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class ShellMongoDataTransportTableListView extends FXListView<FXCheckBox> {

    private Runnable selectedChanged;

    public void of(List<MongoCollection> tables) {
        List<ShellMongoDataTransportCollection> list = CollectionUtil.newArrayList();
        for (MongoCollection table : tables) {
            ShellMongoDataTransportCollection obj = new ShellMongoDataTransportCollection();
            obj.setName(table.getName());
            list.add(obj);
        }
        this.init(list);
    }

    public void init(List<ShellMongoDataTransportCollection> tables) {
        this.clearItems();
        if (CollectionUtil.isNotEmpty(tables)) {
            for (ShellMongoDataTransportCollection table : tables) {
                FXCheckBox checkBox = new FXCheckBox();
                checkBox.setText(table.getName());
                checkBox.setSelected(table.isSelected());
                checkBox.setProp("data", table);
                checkBox.selectedChanged((observable, oldValue, newValue) -> {
                    table.setSelected(newValue);
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

    public List<ShellMongoDataTransportCollection> getSelectedTables() {
        List<ShellMongoDataTransportCollection> list = new ArrayList<>();
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
