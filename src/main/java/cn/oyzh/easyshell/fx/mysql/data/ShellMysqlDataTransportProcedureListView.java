package cn.oyzh.easyshell.fx.mysql.data;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.mysql.procedure.MysqlProcedure;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.util.ListViewUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class ShellMysqlDataTransportProcedureListView extends FXListView<FXCheckBox> {

    private Runnable selectedChanged;

    public Runnable getSelectedChanged() {
        return selectedChanged;
    }

    public void setSelectedChanged(Runnable selectedChanged) {
        this.selectedChanged = selectedChanged;
    }

    public void of(List<MysqlProcedure> procedures) {
        List<ShellMysqlDataTransportProcedure> list = CollectionUtil.newArrayList();
        for (MysqlProcedure procedure : procedures) {
            ShellMysqlDataTransportProcedure obj = new ShellMysqlDataTransportProcedure();
            obj.setName(procedure.getName());
            list.add(obj);
        }
        this.init(list);
    }

    public void init(List<ShellMysqlDataTransportProcedure> procedures) {
        this.clearItems();
        if (CollectionUtil.isNotEmpty(procedures)) {
            for (ShellMysqlDataTransportProcedure procedure : procedures) {
                FXCheckBox checkBox = new FXCheckBox();
                checkBox.setText(procedure.getName());
                checkBox.setSelected(procedure.isSelected());
                checkBox.setProp("data", procedure);
                checkBox.selectedChanged((observable, oldValue, newValue) -> {
                    procedure.setSelected(newValue);
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

    public List<ShellMysqlDataTransportProcedure> getSelectedProcedures() {
        List<ShellMysqlDataTransportProcedure> list = new ArrayList<>();
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
}
