package cn.oyzh.easyshell.data.sqlserver.ui;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.data.sqlserver.dto.ShellSqlServerDataExportColumn;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.util.ListViewUtil;

import java.util.List;

/**
 * SqlServer数据导出字段列表视图
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellSqlServerDataExportColumnListView extends FXListView<FXCheckBox> {

    /**
     * 初始化
     *
     * @param columns 字段列表
     */
    public void init(List<ShellSqlServerDataExportColumn> columns) {
        this.clearItems();
        if (CollectionUtil.isNotEmpty(columns)) {
            for (ShellSqlServerDataExportColumn column : columns) {
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
