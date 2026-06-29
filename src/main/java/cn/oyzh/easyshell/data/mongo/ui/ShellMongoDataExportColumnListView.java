package cn.oyzh.easyshell.data.mongo.ui;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.data.mongo.dto.ShellMongoDataExportColumn;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.util.ListViewUtil;

import java.util.List;

/**
 * @author oyzh
 * @since 2024/08/27
 */
public class ShellMongoDataExportColumnListView extends FXListView<FXCheckBox> {

    public void init(List<ShellMongoDataExportColumn> columns) {
        this.clearItems();
        if (CollectionUtil.isNotEmpty(columns)) {
            for (ShellMongoDataExportColumn column : columns) {
                FXCheckBox checkBox = new FXCheckBox();
                checkBox.setSelected(column.isSelected());
                checkBox.setText(column.getName());
                checkBox.selectedChanged((observable, oldValue, newValue) -> column.setSelected(newValue));
                ListViewUtil.selectRowOnMouseClicked(checkBox);
                this.addItem(checkBox);
            }
        }
    }
}
