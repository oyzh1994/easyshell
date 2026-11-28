package cn.oyzh.easyshell.data.h2.ui;

import cn.oyzh.easyshell.data.h2.dto.H2DataExportColumn;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.util.ListViewUtil;

import java.util.List;

/**
 * H2 数据导出字段选择列表。
 *
 * @since 2026-11-02
 */
public class H2DataExportColumnListView extends FXListView<FXCheckBox> {

    public void init(List<H2DataExportColumn> columns) {
        this.clearItems();
        if (columns != null) {
            for (H2DataExportColumn column : columns) {
                FXCheckBox checkBox = new FXCheckBox();
                checkBox.setSelected(column.isSelected());
                checkBox.setText(column.getName());
                checkBox.selectedChanged((observable, oldValue, newValue) ->
                        column.setSelected(Boolean.TRUE.equals(newValue)));
                ListViewUtil.selectRowOnMouseClicked(checkBox);
                this.addItem(checkBox);
            }
        }
    }
}
