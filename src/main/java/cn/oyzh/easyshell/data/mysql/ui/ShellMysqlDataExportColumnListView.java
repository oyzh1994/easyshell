package cn.oyzh.easyshell.data.mysql.ui;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.data.mysql.dto.ShellMysqlDataExportColumn;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.util.ListViewUtil;

import java.util.List;

/**
 * Mysql数据导出字段列表视图
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlDataExportColumnListView extends FXListView<FXCheckBox> {

    /**
     * 初始化
     *
     * @param columns 字段列表
     */
    public void init(List<ShellMysqlDataExportColumn> columns) {
        this.clearItems();
        if (CollectionUtil.isNotEmpty(columns)) {
            for (ShellMysqlDataExportColumn column : columns) {
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
